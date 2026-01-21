package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class ProductUnitDto {
    @NotBlank(message = "El nombre de la unidad es obligatorio")
    private String unitName;

    @NotNull(message = "El factor de conversión es obligatorio")
    @Positive(message = "El factor debe ser mayor a 0")
    private BigDecimal conversionFactor;

    private String barcode;
}
