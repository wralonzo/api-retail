package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderReceptionRequest {
  @NotNull(message = "La sucursal es obligatoria")
  private Long branchId;

  @NotNull(message = "El almacén de destino es obligatorio")
  private Long warehouseId;

  @NotNull(message = "El proveedor es obligatorio")
  private Long supplierId;

  private String observation; // Notas de la recepción

  @NotEmpty(message = "Debe ingresar al menos un producto")
  private List<ReceptionItemDTO> items;
}