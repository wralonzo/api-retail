package com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceRequest {

    @NotBlank(message = "El número de factura es obligatorio")
    private String invoiceNumber;

    @NotNull(message = "El proveedor es obligatorio")
    private Long supplierId;

    @NotNull(message = "El almacén es obligatorio")
    private Long warehouseId;

    @NotNull(message = "La sucursal es obligatoria")
    private Long branchId;

    @NotNull(message = "La fecha de factura es obligatoria")
    private LocalDateTime invoiceDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDateTime dueDate;

    private String notes;

    @NotEmpty(message = "Debe ingresar al menos un producto")
    @Valid
    private List<InvoiceItemRequest> items;
}
