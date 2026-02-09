package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLoadBulkRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<InventoryLoadRequest> items;

    private Long warehouseId; // Optional default warehouse for all items
}
