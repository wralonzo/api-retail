package com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkOrderDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String notes;
}
