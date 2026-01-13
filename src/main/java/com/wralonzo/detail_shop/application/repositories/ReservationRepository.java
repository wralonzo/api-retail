package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findAll(Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.employee.id = :employee " +
            "AND r.reservationDate = :date " +
            "AND r.state <> com.wralonzo.detail_shop.domain.entities.Reservation.Estado.CANCELADA " +
            "AND (:startTime < r.finishDate AND :finishDate > r.startTime)")
    boolean existsOverlapping(
            @Param("employee") Long employee,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("finishDate") LocalTime finishDate
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.employee.id = :employeeId " +
            "AND r.reservationDate = :date " +
            "AND r.id <> :currentId " +
            "AND r.state <> 'CANCELADA' " +
            "AND (:startTime < r.finishDate AND :finishDate > r.startTime)")
    boolean existsOverlappingUpdate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("finishDate") LocalTime finishDate,
            @Param("currentId") Long currentId
    );
}
