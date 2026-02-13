package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class ProductBundleDto {
    @NotNull(message = "El ID del producto hijo es obligatorio")
    private Long childProductId;

    private String childProductName;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer quantity;
}
