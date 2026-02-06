package com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Quote.QuoteStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuoteResponse {
    private Long id;
    private String referenceNumber;
    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal taxes;
    private BigDecimal total;
    private Long userId;
    private Long warehouseId;
    private Long clientId;
    private QuoteStatus status;
    private String notes;
    private LocalDate dateExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
