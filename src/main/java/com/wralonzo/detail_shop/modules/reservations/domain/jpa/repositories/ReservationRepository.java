package com.wralonzo.detail_shop.modules.reservations.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Page<Reservation> findAll(Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.employeeId = :employeeId " + // Ajustado: ya no es r.employee.id
            "AND r.reservationDate = :date " +
            "AND r.state <> :excludeState " +
            "AND (:startTime < r.finishDate AND :finishDate > r.startTime)")
    boolean existsOverlapping(
            @Param("employeeId") long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("finishDate") LocalTime finishDate,
            @Param("excludeState") Reservation.Estado excludeState
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.employeeId = :employeeId " +
            "AND r.reservationDate = :date " +
            "AND r.id <> :currentId " +
            "AND r.state <> :excludeState " +
            "AND (:startTime < r.finishDate AND :finishDate > r.startTime)")
    boolean existsOverlappingUpdate(
            @Param("employeeId") long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("finishDate") LocalTime finishDate,
            @Param("currentId") Long currentId,
            @Param("excludeState") Reservation.Estado excludeState
    );
}