package com.wralonzo.detail_shop.modules.reservations.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleResponse;
import com.wralonzo.detail_shop.modules.reservations.application.ReservationService;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationRequest;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SaleResponse> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}