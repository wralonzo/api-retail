package com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvoiceItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String batchNumber;
}
