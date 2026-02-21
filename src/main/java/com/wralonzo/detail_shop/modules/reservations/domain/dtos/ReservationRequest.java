package com.wralonzo.detail_shop.modules.reservations.domain.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Employee/Stylist ID is required")
    private Long employeeId;

    private LocalDate reservationDate;

    private LocalTime startTime;

    // Optional expiration date override (otherwise use default)
    private LocalDate expirationDate;

    private String notes;

    @NotEmpty(message = "Reservation must have at least one item")
    private List<ReservationDetailRequest> items;
}
