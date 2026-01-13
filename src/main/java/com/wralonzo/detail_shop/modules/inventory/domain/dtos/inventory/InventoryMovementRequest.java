package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.Data;

@Data
public class InventoryMovementRequest {
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private String type; // ENTRADA, SALIDA, AJUSTE
    private String reference; // Ej: "Compra factura #123"
    private String notes;
    private Long userId;
}