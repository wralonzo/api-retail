package com.wralonzo.detail_shop.modules.reservations.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleResponse;
import com.wralonzo.detail_shop.modules.reservations.application.ReservationService;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationRequest;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAll(
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(reservationService.getAll(pageable, term, clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

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