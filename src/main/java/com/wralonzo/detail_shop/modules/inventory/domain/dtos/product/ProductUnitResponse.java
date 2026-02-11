package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductUnitResponse {
    private Long id;
    private String name;
    private BigDecimal conversionFactor;
    private String barcode;
}
