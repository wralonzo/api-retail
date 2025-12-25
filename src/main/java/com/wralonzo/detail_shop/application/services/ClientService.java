package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.ClientRepository;
import com.wralonzo.detail_shop.application.repositories.UserRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.dto.client.ClientUpdateRequest;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class ClientService {
    private final ClientRepository clientRepository;
    private final UserCreationService userCreationService;
    private final UserRepository userRepository;

    public Client create(ClientRequest payload) {
        Client client = convertToEntity(payload);
        if(payload.getFlagUser()){
            List<String> roles = List.of("ROLE_CLIENTE");

            final UserClient userClient = new UserClient();
            userClient.setClient(client);
            userClient.setRoles(roles);
            userCreationService.SaveClient(userClient);
        }
        this.clientRepository.save(client);
        return client;
    }

    public List<Client> getAll() {
        return this.clientRepository.findAll();
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

    private Client convertToEntity(ClientRequest payload) {
        Client client = new Client();
        client.setName(payload.getName());
        client.setAddress(payload.getAddress());
        client.setEmail(payload.getEmail());
        client.setPhone(payload.getPhone());
        return client;
    }

    @Transactional
    public void update(Long id, ClientUpdateRequest payload) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("Recurso no encontrado: " + id));

        if (payload.getEmail() != null && !payload.getEmail().equalsIgnoreCase(client.getEmail())) {
            if (this.clientRepository.existsByEmail(payload.getEmail())) {
                throw new ResourceConflictException("El email " + payload.getEmail() + " ya está en uso por otro cliente.");
            }
            client.setEmail(payload.getEmail());

            if (client.getUser() != null) {
                client.getUser().setUsername(payload.getEmail());
            }
        }
        if (payload.getName() != null){
            client.setName(payload.getName());
            client.getUser().setFullName(payload.getName());
        }
        if (payload.getPhone() != null){
            client.setPhone(payload.getPhone());
            client.getUser().setFullName(payload.getPhone());
        }
        if (payload.getAddress() != null){
            client.setAddress(payload.getAddress());
            client.getUser().setFullName(payload.getAddress());

        }
        this.clientRepository.save(client);
    }
}
