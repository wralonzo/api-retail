package com.wralonzo.detail_shop.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // Ejemplo: "CHANGE_PASSWORD", "UPDATE_USER"

    @Column(columnDefinition = "TEXT")
    private String description; // Detalles de la acción

    @Column(nullable = false)
    private String username; // Quién realizó la acción

    @CreationTimestamp
    private LocalDateTime timestamp;

    private String ipAddress; // Opcional: para auditoría de seguridad
}