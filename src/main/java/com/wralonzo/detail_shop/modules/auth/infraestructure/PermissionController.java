package com.wralonzo.detail_shop.modules.auth.infraestructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.auth.application.PermissionService;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.permission.PermissionRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Permission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping()
    public ResponseEntity<Page<Permission>> getAll(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        Page<Permission> permissions = this.permissionService.getAll(pageable);
        return ResponseUtil.ok(permissions);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Permission> create(@Valid @RequestBody PermissionRequest request) {
        Permission permission = this.permissionService.create(request);
        return ResponseUtil.ok(permission);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getById(@PathVariable Long id) {
        Permission permission = this.permissionService.getById(id);
        return ResponseUtil.ok(permission);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Permission> update(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        Permission permission = this.permissionService.update(id, request);
        return ResponseUtil.ok(permission);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.permissionService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Permiso eliminado exitosamente"));
    }
}
