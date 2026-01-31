package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

// Para manejar Fardos, Docenas, Six-packs del MISMO producto
@Entity
@Table(name = "product_units_details", schema = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "unit_product_id")
    private ProductUnit unitProduct;
}