package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

// Para manejar Fardos, Docenas, Six-packs del MISMO producto
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String unitName; // "Unidad", "Fardo", "Caja"

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal conversionFactor; // 1 for base, 24 for Fardo

    private String barcode; // Barcode specific for this unit

    @Column(nullable = false)
    private boolean isBase; // True if this is the base unit (factor 1)
}