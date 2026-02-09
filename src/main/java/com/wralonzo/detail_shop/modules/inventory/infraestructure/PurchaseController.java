package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.modules.inventory.application.product.ProcessReceptionService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.PurchaseOrderReceptionRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final ProcessReceptionService processReceptionService;

    /**
     * Procesa la recepción de una orden de compra
     * - Valida proveedor y productos
     * - Actualiza inventario maestro
     * - Crea lotes con información de compra
     * - Registra movimientos en Kardex
     */
    @PostMapping("/reception")
    public ResponseEntity<?> processPurchaseReception(
            @Valid @RequestBody PurchaseOrderReceptionRequest request) {

        try {
            processReceptionService.processReception(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Recepción procesada exitosamente. Inventario actualizado.",
                    "itemsProcessed", request.getItems().size()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al procesar recepción: " + e.getMessage()));
        }
    }
}
