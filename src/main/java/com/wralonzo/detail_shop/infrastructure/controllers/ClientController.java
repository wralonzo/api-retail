package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.ClientService;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientService clientService;
    public ClientController(ClientService clientService){
        this.clientService = clientService;
    }

    @PostMapping("")
    public ResponseEntity<Client> create(@Valid @RequestBody ClientRequest clientRequest){
        Client client = this.clientService.create(clientRequest);
        return ResponseUtil.created(client, client.getId());
    }

    @GetMapping()
    public ResponseEntity<List<Client>> getAll(){
        List<Client> clients = this.clientService.getAll();
        return  ResponseEntity.ok(clients);
    }
}
