package com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WorkOrderDetailRequest {
    @NotNull(message = "El producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    private String notes;
}
