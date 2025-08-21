package com.wralonzo.detail_shop.domain.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ProductUpdateRequest {
    @NotNull(message = "Id is required.")
    @Min(value = 0, message = "Please id positive.")
    private Long id;

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres.")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    private String description;

    @Positive(message = "El precio debe ser mayor a 0.")
    private Double price;

    private Integer stock;
}
