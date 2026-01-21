package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.Data;

@Data
public class BulkPriceAdjustmentRequest {
  private Long warehouseId;
  private Long categoryId;
  private Double percentage; // Ejemplo: 10.5 para subir diez punto cinco por ciento
}