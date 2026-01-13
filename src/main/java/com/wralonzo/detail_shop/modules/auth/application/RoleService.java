package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.RoleProjection;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.RoleRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Builder
public class RoleService {
    private final RoleRepository roleRepository;

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
}
