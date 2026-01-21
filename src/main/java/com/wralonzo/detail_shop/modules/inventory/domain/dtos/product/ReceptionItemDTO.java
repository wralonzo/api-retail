package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceptionItemDTO {
  @NotNull(message = "El ID del producto es obligatorio")
  private Long productId;

  @NotNull(message = "La cantidad es obligatoria")
  @Min(value = 1, message = "La cantidad debe ser mayor a 0")
  private Integer quantity;

  @NotNull(message = "El precio de costo es obligatorio")
  private BigDecimal costPrice;

  private String batchNumber; // Opcional: Número de lote del fabricante

  private LocalDateTime expirationDate; // Opcional: Para productos perecederos
}
