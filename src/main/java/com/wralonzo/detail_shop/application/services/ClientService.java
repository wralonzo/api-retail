package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.ClientRepository;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class ClientService {
    private final ClientRepository clientRepository;

    public Client create(ClientRequest payload) {
        Client client = convertToEntity(payload);
        this.clientRepository.save(client);
        return client;
    }

    public List<Client> getAll(){
        return this.clientRepository.findAll();
    }

    private Client convertToEntity(ClientRequest payload) {
        Client client = new Client();
        client.setName(payload.getName());
        client.setAddress(payload.getAddress());
        client.setEmail(payload.getEmail());
        client.setPhone(payload.getPhone());
        return client;
    }
}
