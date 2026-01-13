package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import com.wralonzo.detail_shop.modules.inventory.domain.enums.StockStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockResponse {
    private String productName;
    private String sku;
    private String warehouseName;
    private Integer currentStock;
    private Integer stockMinim;
    private Integer quantityReserved;
    private Integer quantityAvailable; // La resta de stock - reservados
    private StockStatus status;
}