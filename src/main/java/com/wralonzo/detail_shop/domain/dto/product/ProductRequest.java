package com.wralonzo.detail_shop.domain.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "El campo 'name' es obligatorio y no debe estar vacío.")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres.")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    private String description;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor a 0.")
    private Double price;

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    private Double pricePurchase;

    private Double priceSale;

    private String barcode;

    private Integer stockMinim;

    private String sku;

    private Long categoryId;

    private Long supplierId;

    private Long warehouseId;
}
