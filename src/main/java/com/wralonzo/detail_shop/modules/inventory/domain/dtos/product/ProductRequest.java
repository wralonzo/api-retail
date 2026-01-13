package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "El campo 'name' es obligatorio y no debe estar vacío.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 3 y 50 caracteres.")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    private String description;

    @Positive(message = "El precio debe ser mayor a 0.")
    private BigDecimal pricePurchase;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor a 0.")
    private BigDecimal priceSale;

    private String barcode;

    @Positive(message = "El precio debe ser mayor a 0.")
    private Integer stockMinim;

    private String sku;

    private boolean active;

    @Positive(message = "El precio debe ser mayor a 0.")
    private Long categoryId;
}
