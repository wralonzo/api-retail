package com.wralonzo.detail_shop.modules.organization.application;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.organization.domain.dtos.warehouse.WarehouseRequest;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;
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

    public Page<WarehouseProjection> getAll(Pageable pageable) {
        return warehouseRepository.findAllProjectedBy(pageable);
    }

    public Warehouse getById(long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con ID: " + id));
    }

    public WarehouseProjection getOneRecord(Long id) {
        return warehouseRepository.findGetById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con ID: " + id));
    }

    public WarehouseProjection getByCode(String code) {
        return warehouseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con código: " + code));
    }

    public Map<Long, Warehouse> getWarehousesMap(List<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        return warehouseRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
    }

    @Transactional
    public Warehouse create(WarehouseRequest request) {
        // Verificar que la sucursal existe
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + request.getBranchId()));

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .code(request.getCode())
                .active(request.getActive() != null ? request.getActive() : true)
                .branch(branch)
                .build();

        warehouseRepository.save(warehouse);
        return Warehouse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .phone(warehouse.getPhone())
                .code(warehouse.getCode())
                .build();
    }

    @Transactional
    public Warehouse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = getById(id);

        // Verificar que la sucursal existe si se está cambiando
        if (request.getBranchId() != null && !request.getBranchId().equals(warehouse.getBranch().getId())) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Sucursal no encontrada con ID: " + request.getBranchId()));
            warehouse.setBranch(branch);
        }

        warehouse.setName(request.getName());
        warehouse.setPhone(request.getPhone());
        warehouse.setCode(request.getCode());
        if (request.getActive() != null) {
            warehouse.setActive(request.getActive());
        }

        warehouseRepository.save(warehouse);
        return Warehouse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .phone(warehouse.getPhone())
                .code(warehouse.getCode())
                .build();
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = getById(id);
        warehouseRepository.delete(warehouse);
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

    public List<Long> findAllIdsByCompanyId(Long companyId) {
        return branchRepository.findByCompanyId(companyId).stream()
                .map(Branch::getId)
                .collect(Collectors.toList());
    }
}
