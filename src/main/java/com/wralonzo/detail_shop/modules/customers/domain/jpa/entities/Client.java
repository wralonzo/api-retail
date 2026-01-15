package com.wralonzo.detail_shop.modules.customers.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients", schema = "customer", indexes = {
        // Índice crucial para buscar el perfil de cliente cuando el usuario hace login
        @Index(name = "idx_client_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // Necesario para @CreatedDate y @LastModifiedDate
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN POR ID (Desacoplado) ---
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "profile_id", nullable = false) // Ahora es obligatorio tener perfil
    private Long profileId;

    @Column(name = "company_id", nullable = false, unique = true)
    private Long companyId;

    // --- DATOS ESPECÍFICOS DE CLIENTE ---
    @Column(name = "client_code", unique = true, length = 20)
    private String clientCode;

    @Column(name = "tax_id", length = 20) // NIT / RUT / RFC para facturación
    private String taxId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = true)
    private ClientType clientType = ClientType.REGULAR;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Builder.Default
    private Boolean active = true;

    // Puedes agregar campos que no están en el perfil general
    private String preferredDeliveryAddress;

    private Double creditLimit;

    // --- AUDITORÍA ---
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}