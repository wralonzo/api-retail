package com.wralonzo.detail_shop.domain.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_detail", indexes = {
        @Index(name = "idx_date", columnList = "fecha_movimiento"),
        @Index(name = "idx_type", columnList = "tipo_movimiento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDetail {
    public enum TipoMovimiento {
        ENTRADA, SALIDA, AJUSTE, TRASLADO_SALIDA, TRASLADO_ENTRADA, DEVOLUCION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventory_detail")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_warehouse", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_detail", nullable = false)
    private TipoMovimiento type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "quantity_new", nullable = false)
    private Integer quantityNew;

    @Column(name = "cost_unit", precision = 12, scale = 2)
    private BigDecimal costUnit;

    @Column(length = 100)
    private String reference;

    @Column(name = "id_document")
    private String idDocument;

    @Column(name = "document_type", length = 50)
    private String document_type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at", nullable = true, updatable = false)
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;
}
