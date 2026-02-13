package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStockResponse {
    private String almacenName;
    private StockResponse inventario;
}
