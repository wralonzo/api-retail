package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ProductUnitDto {
    @NotNull(message = "El id de la unidad es obligatorio")
    private Long id;
}
