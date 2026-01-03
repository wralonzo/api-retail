package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.ClientRepository;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserShortResponse;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.domain.entities.Warehouse;
import com.wralonzo.detail_shop.domain.enums.ClientType;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class ClientService {
    private final ClientRepository clientRepository;
    private final UserCreationService userCreationService;
    private final WarehouseRepository warehouseRepository;

    public Client create(ClientRequest payload) {
        Warehouse warehouse = null;
        if (payload.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(payload.getWarehouseId())
                    .orElseThrow(() -> new ResourceConflictException(
                            "El almacén con ID " + payload.getWarehouseId() + " no existe"));
        }
        Client client = convertToEntity(payload, warehouse);
        if (payload.getFlagUser()) {
            List<String> roles = List.of("ROLE_CLIENTE");

            final UserClient userClient = new UserClient();
            userClient.setClient(client);
            userClient.setRoles(roles);
            userCreationService.SaveClient(userClient, true);
        } else {
            if (clientRepository.existsByEmail(payload.getEmail())) {
                throw new ResourceConflictException(
                        "Ya existe un cliente registrado con el correo: " + payload.getEmail());
            }
            String newCode = userCreationService.generateClientCode();
            client.setCode(newCode);
            this.clientRepository.save(client);
        }

        return client;
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getAll(String term, ClientType clientType, Pageable pageable) {
        // Limpiamos el término si viene vacío para que el query lo detecte como NULL
        String cleanTerm = (term != null && !term.trim().isEmpty()) ? term.trim() : null;

        return this.clientRepository
                .searchClients(cleanTerm, clientType, pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public void delete(Long id) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado con ID: " + id));
        if (client.getUser() != null) {
            User user = client.getUser();
            user.setEnabled(false);
            user.setDeletedAt(LocalDateTime.now());
        }
        client.setDeletedAt(LocalDateTime.now());

        this.clientRepository.save(client);
    }

    private Client convertToEntity(ClientRequest payload, Warehouse warehouse) {
        return Client.builder()
                .name(payload.getName())
                .email(payload.getEmail())
                .phone(payload.getPhone())
                .address(payload.getAddress())
                .notes(payload.getNotes())
                .birthDate(payload.getBirthDate())
                .clientType(payload.getClientType())
                .warehouse(warehouse) // Si es null, JPA simplemente guardará un NULL en la DB
                .build();
    }

    @Transactional
    public void update(Long id, ClientRequest payload) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("Recurso no encontrado: " + id));

        if (payload.getEmail() != null && !payload.getEmail().equalsIgnoreCase(client.getEmail())) {
            if (this.clientRepository.existsByEmail(payload.getEmail())) {
                throw new ResourceConflictException(
                        "El email " + payload.getEmail() + " ya está en uso por otro cliente.");
            }
            client.setEmail(payload.getEmail());

            if (client.getUser() != null) {
                client.getUser().setUsername(payload.getEmail());
                client.getUser().setFullName(payload.getName());
                client.getUser().setPhone(payload.getPhone());
                client.getUser().setAddress(payload.getAddress());
                client.getUser().setAddress(payload.getAddress());
            }
        }
        if (payload.getName() != null) {
            client.setName(payload.getName());
        }

        if (payload.getNotes() != null) {
            client.setNotes(payload.getNotes());
        }

        if (payload.getBirthDate() != null) {
            client.setBirthDate(payload.getBirthDate());
        }

        if (payload.getClientType() != null) {
            client.setClientType(payload.getClientType());
        }

        if (payload.getPhone() != null) {
            client.setPhone(payload.getPhone());
        }
        if (payload.getAddress() != null) {
            client.setAddress(payload.getAddress());
        }
        this.clientRepository.save(client);
    }

    public ClientResponse getById(Long id) {
        Client client = this.clientRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado"));
        return convertToResponse(client);
    }

    @Transactional
    public ClientResponse createUser(Long id) {

        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado"));

        List<String> roles = List.of("ROLE_CLIENTE");

        final UserClient userClient = new UserClient();
        userClient.setClient(client);
        userClient.setRoles(roles);
        User userCreated = userCreationService.SaveClient(userClient, false);
        client.setUser(userCreated);
        return convertToResponse(client);

    }

    public ClientResponse convertToResponse(Client client) {
        UserShortResponse userDto = null;
        if (client.getUser() != null) {
            userDto = UserShortResponse.builder()
                    .id(client.getUser().getId())
                    .username(client.getUser().getUsername())
                    .fullName(client.getUser().getFullName())
                    .passwordInit(client.getUser().getPasswordInit())
                    .roles(client.getUser().getRoles().stream()
                            .map(role -> role.getName())
                            .toList())
                    .build();
        }
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .notes(client.getNotes())
                .code(client.getCode())
                .birthDate(client.getBirthDate())
                .clientType(client.getClientType().name())
                .warehouseId(client.getWarehouse() != null ? client.getWarehouse().getId() : null)
                .user(userDto)
                .build();
    }

}
