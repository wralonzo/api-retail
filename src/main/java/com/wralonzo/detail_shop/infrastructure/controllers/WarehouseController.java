package com.wralonzo.detail_shop.infrastructure.controllers;


import com.wralonzo.detail_shop.application.projections.WarehouseProjection;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.application.services.WarehouseService;
import com.wralonzo.detail_shop.domain.dto.warehouse.WarehouseResponseDTO;
import com.wralonzo.detail_shop.domain.entities.Warehouse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
@AllArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping()
    public ResponseEntity<List<WarehouseProjection>> getAll(){
        List<WarehouseProjection> warehouses = this.warehouseService.getAll();
        return ResponseEntity.ok(warehouses);
    }
}
