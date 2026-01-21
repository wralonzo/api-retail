package com.wralonzo.detail_shop.modules.reservations.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {
    private Long productId;
    private String productName;
    private Long unitId;
    private String unitName;
    private Integer quantity;
    private BigDecimal priceUnit;
    private BigDecimal subtotal;
}
