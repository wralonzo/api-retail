package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.ClientService;
import com.wralonzo.detail_shop.domain.dto.client.ClientRequest;
import com.wralonzo.detail_shop.domain.dto.client.ClientUpdateRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client")
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

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')") // Solo un admin debería poder desactivar usuarios
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Recurso eliminado exitosamente"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientUpdateRequest> update(@PathVariable Long id, @RequestBody ClientUpdateRequest request) {
        clientService.update(id, request);
        return ResponseEntity.ok(request);
    }
}
