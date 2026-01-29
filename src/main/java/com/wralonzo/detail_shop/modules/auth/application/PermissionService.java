package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.permission.PermissionRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Permission;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PermissionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public Page<Permission> getAll(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Transactional
    public Permission create(PermissionRequest request) {
        // Verificar que no exista un permiso con el mismo nombre
        permissionRepository.findByName(request.getName())
                .ifPresent(p -> {
                    throw new ResourceConflictException("Ya existe un permiso con el nombre: " + request.getName());
                });

        Permission permission = Permission.builder()
                .name(request.getName())
                .build();

        return permissionRepository.save(permission);
    }

    public Permission getById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));
    }

    @Transactional
    public Permission update(Long id, PermissionRequest request) {
        Permission permission = getById(id);

        // Verificar que no exista otro permiso con el mismo nombre
        permissionRepository.findByName(request.getName())
                .ifPresent(p -> {
                    if (!p.getId().equals(id)) {
                        throw new ResourceConflictException(
                                "Ya existe otro permiso con el nombre: " + request.getName());
                    }
                });

        permission.setName(request.getName());

        return permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = getById(id);
        permissionRepository.delete(permission);
    }
}
