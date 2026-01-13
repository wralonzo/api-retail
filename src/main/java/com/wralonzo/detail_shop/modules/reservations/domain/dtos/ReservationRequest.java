package com.wralonzo.detail_shop.modules.reservations.domain.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationRequest {
    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El almacén es obligatorio")
    private Long warehouseId;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha de reserva no puede ser en el pasado")
    private LocalDate reservationDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalTime finishDate;

    @NotBlank(message = "El tipo de reserva es obligatorio")
    private String type;

    private String notes;

    @NotNull(message = "El empleado asignado es obligatorio")
    private Long employee;
}
