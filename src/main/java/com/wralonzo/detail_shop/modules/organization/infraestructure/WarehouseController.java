package com.wralonzo.detail_shop.modules.organization.infraestructure;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;

import java.util.List;

@RequestMapping("/warehouse")
@RequiredArgsConstructor
@RestController
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping()
    public ResponseEntity<List<WarehouseProjection>> getAll(){
        List<WarehouseProjection> warehouses = this.warehouseService.getAll();
        return ResponseEntity.ok(warehouses);
    }
}
