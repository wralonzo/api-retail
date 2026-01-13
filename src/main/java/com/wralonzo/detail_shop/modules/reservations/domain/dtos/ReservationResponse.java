package com.wralonzo.detail_shop.modules.reservations.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private String clientName;
    private String employeeName;
    private String warehouseName;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime finishDate;
    private String type;
    private String state;
    private String notes;
}