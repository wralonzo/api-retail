package com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrder.WorkOrderStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class WorkOrderRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El almacén es obligatorio")
    private Long warehouseId;

    private WorkOrderStatus status;
    private String notes;

    private List<WorkOrderDetailRequest> details;
}
