package com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private BigDecimal discount;
}
