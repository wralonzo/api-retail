package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KardexResponse {
    private LocalDateTime date;
    private String type;        // ENTRADA, SALIDA, AJUSTE
    private Integer quantity;
    private String reference;   // # Factura, # Ticket, etc.
    private String notes;
    private String username;    // Quién hizo el movimiento
}