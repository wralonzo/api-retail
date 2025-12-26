package com.wralonzo.detail_shop.domain.dto.reservation;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationRequest {
    private Long clientId;
    private Long warehouseId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime finishDate;
    private String type;
    private String notes;
    private Long employee;
}
