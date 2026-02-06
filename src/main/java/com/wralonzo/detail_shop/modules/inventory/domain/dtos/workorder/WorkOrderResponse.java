package com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrder.WorkOrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkOrderResponse {
    private Long id;
    private String referenceNumber;
    private String name;
    private String description;
    private Long userId;
    private Long clientId;
    private Long warehouseId;
    private WorkOrderStatus status;
    private String notes;
    private List<WorkOrderDetailResponse> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
