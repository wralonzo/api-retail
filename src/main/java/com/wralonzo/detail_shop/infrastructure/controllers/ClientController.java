package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.ClientService;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.enums.ClientType;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("")
    public ResponseEntity<Client> create(@Valid @RequestBody ClientRequest clientRequest) {
        Client client = this.clientService.create(clientRequest);
        return ResponseUtil.created(client, client.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        ClientResponse client = this.clientService.getById(id);
        return ResponseUtil.ok(client);
    }

    @GetMapping()
    public ResponseEntity<Page<ClientResponse>> getAll(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) ClientType clientType,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<ClientResponse> clients = this.clientService.getAll(term, clientType, pageable);
        return ResponseUtil.ok(clients);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        clientService.update(id, request);
        return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
    }

    @GetMapping("/{id}/user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ClientResponse> createUser(@PathVariable(required = true) Long id) {
        ClientResponse client = this.clientService.createUser(id);
        return ResponseUtil.ok(client);
    }
}
