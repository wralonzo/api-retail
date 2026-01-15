package com.wralonzo.detail_shop.modules.customers.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.auth.application.AuthService;
import com.wralonzo.detail_shop.modules.auth.application.ProfileService;
import com.wralonzo.detail_shop.modules.auth.application.UserClientService;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserAuthDto;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.FullClientCreateRequest;
import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories.ClientRepository;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.specs.ClientSpecifications;
import com.wralonzo.detail_shop.modules.customers.domain.mapper.ClientMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.wralonzo.detail_shop.modules.organization.application.CompanyService;

@Service
@AllArgsConstructor
@Builder
public class ClientService {
    private final ClientRepository clientRepository;
    private final AuthService authService;
    private final UserClientService userClientService;
    private final ClientMapper clientMapper;
    private final ProfileService profileService;
    private final CompanyService companyService;

    @Transactional // Importante: si falla la creación del cliente, debe revertirse el usuario
    public ClientResponse createFullClient(FullClientCreateRequest request) {
        Long userCompanyId = companyService.getCurrentUserCompanyId();

        if (userCompanyId != null) {
            companyService.getById(userCompanyId);
        }

        User userNew = null;
        Profile profile = null;

        if (request.isFlagUser()) {
            userNew = userClientService.createClient(request.getAuth());
            profile = userNew.getProfile();
        } else {
            // Suponiendo que profileService.save devuelve la ENTIDAD Profile
            profile = profileService.save(request.getAuth());
        }

        String clientCode = generateClientCode();

        Client client = Client.builder()
                .userId(userNew != null ? userNew.getId() : null)
                .profileId(profile.getId())
                .code(clientCode) // Solo una vez
                .clientType(request.getClient().getClientType())
                .companyId(userCompanyId)
                .preferredDeliveryAddress(request.getClient().getPreferredDeliveryAddress())
                .taxId(request.getClient().getTaxId())
                .active(true)
                .build();

        Client savedClient = clientRepository.save(client);

        // Usamos la versión del mapper que recibe (Client, Profile, User)
        return clientMapper.toResponse(savedClient, profile, userNew);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchFullClients(String term, ClientType type, Pageable pageable) {
        Long userCompanyId = companyService.getCurrentUserCompanyId();

        List<Long> profileIdsFromAuth = (term != null && !term.isBlank())
                ? profileService.findIdsByTerm(term)
                : Collections.emptyList();

        /// 2. Combinar especificaciones
        Specification<Client> spec = Specification
                .where(ClientSpecifications.isNotDeleted())
                .and(ClientSpecifications.hasClientType(type))
                .and(ClientSpecifications.hasCompanyId(userCompanyId))
                .and(ClientSpecifications.containsTerm(term, profileIdsFromAuth));

        Page<Client> clientsPage = clientRepository.findAll(spec, pageable);

        // 3. Carga masiva para el Mapper (Evita N+1)
        List<Long> profileIds = clientsPage.getContent().stream().map(Client::getProfileId).toList();
        List<Long> userIds = clientsPage.getContent().stream()
                .map(Client::getUserId).filter(java.util.Objects::nonNull).toList();

        Map<Long, Profile> profileMap = profileService.getProfilesMap(profileIds);
        Map<Long, UserAuthDto> authMap = authService.getUsersAuthData(userIds);

        // 4. Mapeo final
        return clientsPage.map(client -> {
            Profile profile = profileMap.get(client.getProfileId());
            UserAuthDto authData = authMap.get(client.getUserId());
            return clientMapper.toResponse(client, profile, authData);
        });
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        Client client = findOneById(id);
        Profile profile = null;
        User user = null;
        if (client.getUserId() != null) {
            User userExists = authService.findById(client.getUserId());
            profile = userExists.getProfile();

        } else {
            profile = profileService.getById(client.getProfileId());
        }

        return clientMapper.toResponse(client, profile, user);
    }

    @Transactional
    public void delete(Long id) {
        Client client = findOneById(id);
        if (client.getUserId() != null) {
            userClientService.deleteUser(client.getId());
        }
        if (client.getProfileId() != null) {
            profileService.delete(client.getProfileId());
        }
        client.setActive(false);
        client.setDeletedAt(LocalDateTime.now());
        this.clientRepository.save(client);
    }

    @Transactional
    public ClientResponse update(Long id, FullClientCreateRequest request) {
        Client client = findOneById(id);

        // 1. Actualizar perfil
        Profile profile = profileService.update(client.getProfileId(), request.getAuth());

        // 2. Actualizar negocio
        if (request.getClient().getClientType() != null)
            client.setClientType(request.getClient().getClientType());
        if (request.getClient().getTaxId() != null)
            client.setTaxId(request.getClient().getTaxId());
        if (request.getClient().getPreferredDeliveryAddress() != null) {
            client.setPreferredDeliveryAddress(request.getClient().getPreferredDeliveryAddress());
        }

        this.clientRepository.save(client);

        // 3. Cargar usuario si existe (Entidad)
        User user = (client.getUserId() != null) ? userClientService.searchById(client.getUserId()) : null;

        // Aquí se resuelve tu error: enviamos (Client, Profile, User)
        return clientMapper.toResponse(client, profile, user);

    }

    @Transactional
    public ClientResponse createUser(Long id) {
        Client client = findOneById(id);

        Profile profile = this.profileService.getById(client.getProfileId());
        User user = this.userClientService.createUser(profile);
        client.setUserId(user.getId());
        client.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(client);

        return clientMapper.toResponse(client, profile, user);

    }

    @Transactional(readOnly = true)
    private Client findOneById(Long id) {
        Long userCompanyId = companyService.getCurrentUserCompanyId();
        Specification<Client> spec = Specification
                .where(ClientSpecifications.isNotDeleted())
                .and(ClientSpecifications.hasCompanyId(userCompanyId))
                .and((root, query, cb) -> cb.equal(root.get("id"), id));

        return clientRepository.findOne(spec)
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado"));
    }

    public String generateClientCode() {
        String prefix = "CLI-";
        int defaultStart = 1;

        // 1. Buscar el último código en la DB
        return clientRepository.findFirstByOrderByCodeDesc()
                .map(lastClient -> {
                    // 2. Extraer el número del código (ej: de "CLI-0005" obtener "0005")
                    String lastCode = lastClient.getCode();
                    String numericPart = lastCode.substring(prefix.length());

                    // 3. Incrementar el número
                    int nextNumber = Integer.parseInt(numericPart) + 1;

                    // 4. Formatear de nuevo con ceros a la izquierda (CLI-0006)
                    return prefix + String.format("%04d", nextNumber);
                })
                // Si no hay clientes, empezamos con el primero
                .orElse(prefix + String.format("%04d", defaultStart));
    }

}
