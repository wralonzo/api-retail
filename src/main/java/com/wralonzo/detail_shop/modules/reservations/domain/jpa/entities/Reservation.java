package com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_date", columnList = "reservation_date"),
        @Index(name = "idx_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    public enum Estado {
        PROGRAMADA, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA, NO_ASISTIO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Long id;


    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "finish_date", nullable = false)
    private LocalTime finishDate;

    @Column(name = "type", length = 100)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado state = Estado.PROGRAMADA;

    @CreatedDate
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at", updatable = false)
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
}
