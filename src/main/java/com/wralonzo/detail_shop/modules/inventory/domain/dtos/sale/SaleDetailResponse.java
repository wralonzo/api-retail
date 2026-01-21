package com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailResponse {
    private Long productId;
    private String productName;
    private Long unitId;
    private String unitName;
    private Integer quantity;
    private BigDecimal priceUnit;
    private BigDecimal discount;
    private BigDecimal subtotal;
}
