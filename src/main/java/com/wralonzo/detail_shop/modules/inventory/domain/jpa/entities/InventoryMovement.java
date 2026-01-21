package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements", schema = "inventory")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_batch") // Opcional, pero vital para saber qué lote se movió
    private InventoryBatch batch;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity; // Siempre positivo. El tipo de movimiento define si suma o resta.

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock; // Stock en el almacén ANTES del movimiento

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock; // Stock en el almacén DESPUÉS del movimiento

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice; // Costo del producto al momento del movimiento

    @Column(name = "user_id", nullable = false)
    private Long userId; // Quién realizó la operación

    @Column(columnDefinition = "TEXT")
    private String reference; // Ej: "Factura #123", "Orden de Compra #45", "Ajuste por rotura"

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}