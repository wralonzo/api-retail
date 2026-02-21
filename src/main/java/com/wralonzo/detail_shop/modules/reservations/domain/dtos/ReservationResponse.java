package com.wralonzo.detail_shop.modules.reservations.domain.dtos;

import com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities.Reservation.Estado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long clientId;
    private Long warehouseId;
    private Long employeeId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime finishDate;
    private LocalDateTime expirationDate;
    private Estado state;
    private BigDecimal total;
    private String notes;
    private List<ReservationDetailResponse> details;
}