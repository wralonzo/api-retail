package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_batch", schema = "inventory")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatch {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_product", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_supplier", nullable = false)
  private Supplier supplier;

  @Column(name = "warehouse_id", nullable = false)
  private Long warehouseId; // Ubicación física exacta (Bodega A, Bodega B, etc.)

  @Column(name = "batch_number", length = 50)
  private String batchNumber;

  @Column(name = "initial_quantity", nullable = false)
  private Integer initialQuantity;

  @Column(name = "current_quantity", nullable = false)
  private Integer currentQuantity;

  @Column(name = "cost_price", precision = 12, scale = 2)
  private BigDecimal costPrice;

  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}