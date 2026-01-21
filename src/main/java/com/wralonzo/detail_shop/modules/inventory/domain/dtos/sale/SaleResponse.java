package com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Sale.Estado;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Sale.TipoVenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private String prefix;
    private Long clientId;
    private String clientName;
    private Long warehouseId;
    private LocalDateTime saleDate;
    private TipoVenta type;
    private String notes;
    private Estado state;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxes;
    private BigDecimal total;

    private List<SaleDetailResponse> details;
}
