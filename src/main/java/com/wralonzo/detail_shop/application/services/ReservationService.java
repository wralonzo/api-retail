package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.ClientRepository;
import com.wralonzo.detail_shop.application.repositories.EmployeeRepository;
import com.wralonzo.detail_shop.application.repositories.ReservationRepository;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.reservation.ReservationRequest;
import com.wralonzo.detail_shop.domain.dto.reservation.ReservationResponse;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.Reservation;
import com.wralonzo.detail_shop.domain.entities.Warehouse;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@AllArgsConstructor
@Builder
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final WarehouseRepository warehouseRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAll(Pageable pageable) {
        Page<Reservation> entitiesPage = reservationRepository.findAll(pageable);
        entitiesPage.getContent();
        return entitiesPage.map(this::mapToResponse);
    }

    @Transactional
    public void create(ReservationRequest payload) {
        if (payload.getFinishDate().isBefore(payload.getStartTime())) {
            throw new ResourceConflictException("La hora de finalización no puede ser antes de la hora de inicio.");
        }
        // 2. Verificar disponibilidad (Solapamiento)
        boolean isOccupied = reservationRepository.existsOverlapping(
                payload.getEmployee(),
                payload.getReservationDate(),
                payload.getStartTime(),
                payload.getFinishDate()
        );

        if (isOccupied) {
            throw new ResourceConflictException(
                    "El empleado " + payload.getEmployee() + " ya tiene una cita programada en ese horario."
            );
        }

        Client client = clientRepository.findById(payload.getClientId())
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado"));

        Warehouse warehouse = warehouseRepository.findById(payload.getWarehouseId())
                .orElseThrow(() -> new ResourceConflictException("Sucursal no encontrada"));

        Employee employee = employeeRepository.findById(payload.getEmployee())
                .orElseThrow(() -> new ResourceConflictException("Empleado no encontrada"));

        Reservation reservation = Reservation.builder()
                .client(client)
                .warehouse(warehouse)
                .reservationDate(payload.getReservationDate())
                .startTime(payload.getStartTime())
                .finishDate(payload.getFinishDate())
                .type(payload.getType())
                .notes(payload.getNotes())
                .employee(employee)
                .state(Reservation.Estado.PROGRAMADA)
                .build();
        reservationRepository.save(reservation);
    }

    public ReservationResponse getById(Long id) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));
        return mapToResponse(res);
    }

    // --- ACTUALIZAR (PATCH)
    @Transactional
    public ReservationResponse update(Long id, ReservationRequest payload) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));

        // Si cambia el horario o el empleado, validamos solapamiento de nuevo
        boolean needsOverlapCheck = false;
        if (payload.getEmployee() != null && !payload.getEmployee().equals(res.getEmployee().getId())) {
            Employee newEmployee = employeeRepository.findById(payload.getEmployee()).orElseThrow();
            res.setEmployee(newEmployee);
            needsOverlapCheck = true;
        }

        if (payload.getReservationDate() != null) {
            res.setReservationDate(payload.getReservationDate());
            needsOverlapCheck = true;
        }
        if (payload.getStartTime() != null) {
            res.setStartTime(payload.getStartTime());
            needsOverlapCheck = true;
        }
        if (payload.getFinishDate() != null) {
            res.setFinishDate(payload.getFinishDate());
            needsOverlapCheck = true;
        }

        if (needsOverlapCheck) {
            validateOverlap(res.getEmployee().getId(), res.getReservationDate(), res.getStartTime(), res.getFinishDate(), res.getId());
        }

        if (payload.getNotes() != null) res.setNotes(payload.getNotes());
        if (payload.getType() != null) res.setType(payload.getType());

        return mapToResponse(reservationRepository.save(res));
    }

    // --- ELIMINAR (SOFT DELETE)
    @Transactional
    public void delete(Long id) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));
        res.setDeletedAt(LocalDateTime.now());
        res.setState(Reservation.Estado.CANCELADA); // Opcional: marcar como cancelada al borrar
        reservationRepository.save(res);
    }

    // --- MÉTODOS DE APOYO (Helpers)
    private void validateOverlap(Long empId, LocalDate date, LocalTime start, LocalTime end, Long currentId) {
        // Nota: Aquí podrías necesitar un query que ignore el ID actual de la cita para permitir editar sin chocar con sigo misma
        boolean occupied = reservationRepository.existsOverlappingUpdate(empId, date, start, end, currentId);
        if (occupied) throw new ResourceConflictException("El nuevo horario choca con otra cita existente.");
    }

    private ReservationResponse mapToResponse(Reservation res) {
        return ReservationResponse.builder()
                .id(res.getId())
                .clientName(res.getClient().getName())
                .employeeName(res.getEmployee() != null ? res.getEmployee().getUser().getFullName() : "Sin asignar")
                .warehouseName(res.getWarehouse().getName())
                .reservationDate(res.getReservationDate())
                .startTime(res.getStartTime())
                .finishDate(res.getFinishDate())
                .state(res.getState().name())
                .type(res.getType())
                .notes(res.getNotes())
                .build();
    }
}
