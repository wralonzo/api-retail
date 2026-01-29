package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.role.AssignPermissionsRequest;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.role.RoleRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Permission;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.RoleProjection;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PermissionRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.RoleRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Builder
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public Set<Role> getRolesFromRequest(List<String> roleNames) {
        Set<Role> roles = roleRepository.findAllByNameIn(roleNames)
                .orElseThrow(() -> new ResourceNotFoundException("Algunos roles no fueron encontrados"));
        if (roles.size() != roleNames.size()) {
            throw new ResourceConflictException("La lista de roles contiene nombres inválidos");
        }

        return roles;
    }

    public Page<RoleProjection> find(Pageable pageable) {
        return this.roleRepository.findAllProjectedBy(pageable);
    }

    @Transactional
    public Role create(RoleRequest request) {
        // Verificar que no exista un rol con el mismo nombre
        roleRepository.findByName(request.getName())
                .ifPresent(r -> {
                    throw new ResourceConflictException("Ya existe un rol con el nombre: " + request.getName());
                });

        Role role = Role.builder()
                .name(request.getName())
                .note(request.getNote())
                .permissions(new HashSet<>())
                .build();

        return roleRepository.save(role);
    }

    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
    }

    @Transactional
    public Role update(Long id, RoleRequest request) {
        Role role = getById(id);

        // Verificar que no exista otro rol con el mismo nombre
        roleRepository.findByName(request.getName())
                .ifPresent(r -> {
                    if (!r.getId().equals(id)) {
                        throw new ResourceConflictException("Ya existe otro rol con el nombre: " + request.getName());
                    }
                });

        role.setName(request.getName());
        role.setNote(request.getNote());

        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = getById(id);
        // Soft delete: podrías agregar un campo deletedAt si lo necesitas
        // Por ahora, simplemente eliminamos el rol
        roleRepository.delete(role);
    }

    @Transactional
    public Role assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = getById(roleId);

        // Obtener los permisos por IDs
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());

        if (permissions.size() != request.getPermissionIds().size()) {
            throw new ResourceNotFoundException("Algunos permisos no fueron encontrados");
        }

        // Agregar los permisos al rol (sin eliminar los existentes)
        role.getPermissions().addAll(permissions);

        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = getById(roleId);

        // Obtener los permisos por IDs
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());

        // Remover los permisos del rol
        role.getPermissions().removeAll(permissions);

        return roleRepository.save(role);
    }
}
