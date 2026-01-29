package com.wralonzo.detail_shop.modules.organization.infraestructure;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.modules.organization.domain.dtos.warehouse.WarehouseRequest;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;

import java.util.List;
import java.util.Map;

@RequestMapping("/warehouse")
@RequiredArgsConstructor
@RestController
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping()
    public ResponseEntity<Page<WarehouseProjection>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<WarehouseProjection> warehouses = this.warehouseService.getAll(pageable);
        return ResponseUtil.ok(warehouses);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Warehouse> create(@Valid @RequestBody WarehouseRequest request) {
        Warehouse warehouse = this.warehouseService.create(request);
        return ResponseUtil.ok(warehouse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseProjection> getOne(@PathVariable Long id) {
        WarehouseProjection warehouse = this.warehouseService.getOneRecord(id);
        return ResponseUtil.ok(warehouse);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Warehouse> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        Warehouse warehouse = this.warehouseService.update(id, request);
        return ResponseUtil.ok(warehouse);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.warehouseService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Almacén eliminado exitosamente"));
    }
}
