package com.wralonzo.detail_shop.modules.organization.application;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;

import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.BranchRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    public List<WarehouseProjection> getAll() {
        return this.warehouseRepository.findAllProjectedBy();
    }

    public Warehouse getById(long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("No existe el almacén"));
    }

    public WarehouseProjection getOneRecord(Long id) {
        return warehouseRepository.findGetById(id)
                .orElseThrow(() -> new ResourceConflictException("No existe el almacén"));
    }

    public WarehouseProjection getByCode(String code) {
        return warehouseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceConflictException("No existe el almacén"));
    }

    public Map<Long, Warehouse> getWarehousesMap(List<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        return warehouseRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public UserBusinessContext getUserBusinessContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User userPrincipal)) {
            throw new ResourceUnauthorizedException("Sesión no válida");
        }

        // 1. Verificar si es SUPER_ADMIN
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return new UserBusinessContext(null, Collections.emptyList(), true, null, null);
        }

        // 2. Obtener el usuario y su punto de anclaje (Warehouse)
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceUnauthorizedException("Usuario no encontrado"));

        if (user.getEmployee() == null || user.getEmployee().getWarehouseId() == null) {
            throw new ResourceConflictException("El usuario no tiene asignado un centro de trabajo.");
        }

        // 3. Obtener la compañía a través del almacén del empleado
        Warehouse userWarehouse = getById(user.getEmployee().getWarehouseId());
        Long companyId = branchRepository.findById(userWarehouse.getBranch().getId())
                .map(branch -> branch.getCompany().getId())
                .orElseThrow(() -> new ResourceConflictException("Error en jerarquía de compañía"));

        // 4. IMPORTANTE: Obtener TODOS los almacenes que pertenecen a esa compañía
        // Necesitas este método en tu WarehouseRepository
        List<Long> allWarehouseIds = warehouseRepository.findAllIdsByCompanyId(companyId);

        return new UserBusinessContext(companyId, allWarehouseIds, false, user, userWarehouse.getBranch().getId());
    }
}
