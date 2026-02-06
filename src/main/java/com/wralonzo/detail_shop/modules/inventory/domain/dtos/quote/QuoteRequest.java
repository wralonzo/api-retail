package com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Quote.QuoteStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuoteRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El monto es obligatorio")
    @PositiveOrZero(message = "El monto no puede ser negativo")
    private BigDecimal amount;

    @PositiveOrZero(message = "El descuento no puede ser negativo")
    private BigDecimal discount;

    @PositiveOrZero(message = "Los impuestos no pueden ser negativos")
    private BigDecimal taxes;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser mayor a 0")
    private BigDecimal total;

    @NotNull(message = "El almacén es obligatorio")
    private Long warehouseId;

    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    private QuoteStatus status;
    private String notes;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate dateExpired;
}
