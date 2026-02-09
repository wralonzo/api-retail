package com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Invoice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private Long branchId;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Invoice.InvoiceStatus status;
    private String notes;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
