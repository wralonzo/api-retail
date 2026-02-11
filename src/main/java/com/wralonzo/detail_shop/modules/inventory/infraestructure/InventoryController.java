package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryMovementService;
import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryLoadBulkRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryLoadRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryMovementRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMovementService inventoryMovementService;

    // Registrar una entrada, salida o ajuste
    @PostMapping("/movement")
    public ResponseEntity<?> processMovement(@Valid @RequestBody InventoryMovementRequest request) {
        inventoryMovementService.processGenericMovement(request);
        return ResponseEntity.ok(Map.of("message", "Movimiento de inventario procesado con éxito"));
    }

    // Consultar stock de un producto específico en una sucursal
    @GetMapping("/stock")
    public ResponseEntity<?> getStock(
            @RequestParam @NotNull @Positive Long productId,
            @RequestParam @NotNull @Positive Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getCurrentStock(productId, warehouseId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    // Load inventory - Individual
    @PostMapping("/load")
    public ResponseEntity<?> loadInventory(
            @Valid @RequestBody InventoryLoadRequest request) {
        var response = inventoryService.loadInventory(request);
        return ResponseEntity.ok(response);
    }

    // Load inventory - Bulk
    @PostMapping("/load/bulk")
    public ResponseEntity<?> loadInventoryBulk(
            @Valid @RequestBody InventoryLoadBulkRequest request) {
        var response = inventoryService.loadInventoryBulk(request);
        return ResponseEntity.ok(response);
    }

    // Load inventory - From Excel file
    @PostMapping("/load/excel")
    public ResponseEntity<?> loadInventoryFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long warehouseId) {
        var response = inventoryService.loadInventoryFromExcel(file, warehouseId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/initialize-branch/{branchId}")
    public ResponseEntity<?> initializeBranchInventory(@PathVariable Long branchId) {
        inventoryService.initializeBranchInventory(branchId);
        return ResponseEntity.ok(Map.of("message", "Inventario de sucursal inicializado con éxito"));
    }

}
