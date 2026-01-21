package com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Sale.TipoVenta;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @Builder.Default
    private TipoVenta type = TipoVenta.CONTADO;

    private String notes;

    @NotEmpty(message = "Sale must have at least one item")
    private List<SaleDetailRequest> items;
}
