package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkInventoryRequest {

    @NotNull(message = "El ID del almacén es obligatorio")
    private Long warehouseId;

    private Long categoryId;
}
