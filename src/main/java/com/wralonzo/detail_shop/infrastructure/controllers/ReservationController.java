package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.ReservationService;
import com.wralonzo.detail_shop.domain.dto.reservation.ReservationRequest;
import com.wralonzo.detail_shop.domain.dto.reservation.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReservationRequest request) {
        reservationService.create(request);
        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAll(
            @PageableDefault(page = 0, size = 10, sort = "reservationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        // Ejemplo de uso: GET /api/reservations?page=0&size=5&sort=startTime,asc
        return ResponseEntity.ok(reservationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Recurso eliminado exitosamente"));
    }
}