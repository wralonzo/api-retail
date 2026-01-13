package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.InventoryService;
import com.wralonzo.detail_shop.domain.dto.inventory.BulkInventoryRequest;
import com.wralonzo.detail_shop.domain.dto.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.domain.dto.inventory.KardexResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Registrar una entrada, salida o ajuste
    @PostMapping("/movement")
    public ResponseEntity<?> processMovement(@Valid @RequestBody InventoryMovementRequest request) {
        inventoryService.processMovement(request);
        return ResponseEntity.ok(Map.of("message", "Movimiento de inventario procesado con éxito"));
    }

    // Consultar stock de un producto específico en una sucursal
    @GetMapping("/stock")
    public ResponseEntity<?> getStock(
            @RequestParam @NotNull @Positive Long productId,
            @RequestParam @NotNull @Positive Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getCurrentStock(productId, warehouseId));
    }

    @PostMapping("/bulk-initialize")
    public ResponseEntity<?> bulkInitialize(@Valid @RequestBody BulkInventoryRequest request) {
        inventoryService.bulkInitialize(request);
        return ResponseEntity.ok(Map.of("message", "Proceso ejecutado con exito"));
    }

    @GetMapping("/kardex")
    public ResponseEntity<?> getKardex(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<KardexResponse> report = inventoryService.getKardex(productId, warehouseId, startDate, endDate);

        if (report.isEmpty()) {
            return ResponseEntity.ok(Map.of());
        }

        return ResponseEntity.ok(report);
    }
}
