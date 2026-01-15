package com.wralonzo.detail_shop.modules.customers.infraestructure;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.customers.application.ClientService;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.FullClientCreateRequest;
import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;
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
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody FullClientCreateRequest request) {
        ClientResponse clientResponse = this.clientService.createFullClient(request);
        return ResponseUtil.created(clientResponse, clientResponse.getId());
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
        Page<ClientResponse> clients = this.clientService.searchFullClients(term, clientType, pageable);
        return ResponseUtil.ok(clients);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
            @Valid @RequestBody FullClientCreateRequest request) {
        ClientResponse client = clientService.update(id, request);
        return ResponseUtil.ok(client);
    }

    @GetMapping("/{id}/user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClientResponse> createUser(@PathVariable(required = true) Long id) {
        ClientResponse client = this.clientService.createUser(id);
        return ResponseUtil.ok(client);
    }
}
