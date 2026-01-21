package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;

@Data
@Builder
public class KardexResponse {
    private LocalDateTime date;
    private String type; // ENTRADA, SALIDA, AJUSTE, TRASLADO
    private Integer quantity; // Cantidad de la variación (ej: +10 o 10)
    private BigDecimal costPrice;; // Cantidad de la variación (ej: +10 o 10)

    // --- NUEVOS CAMPOS PARA TRAZABILIDAD ---
    private Integer previousStock; // Saldo antes del movimiento (quantityAfter en entidad)
    private Integer currentStock; // Saldo antes del movimiento (quantityAfter en entidad)
    private Integer newBalance; // Saldo después del movimiento (quantityNew en entidad)

    private String reference; // # Factura, # Lote, # Ticket
    private String notes;
    private MovementType movementType;

}