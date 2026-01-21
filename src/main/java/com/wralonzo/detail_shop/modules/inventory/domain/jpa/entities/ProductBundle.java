package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

// Para manejar mezclas de DIFERENTES productos (Combos)
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_product_id", nullable = false)
    private Product comboProduct; // El SKU del Combo

    @ManyToOne
    @JoinColumn(name = "child_product_id", nullable = false)
    private Product componentProduct; // El producto real que descuenta stock

    @Column(nullable = false)
    private Integer quantity; // Cuántos incluye
}