package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale_detail")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sale", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product_unit", nullable = false)
    private ProductUnit unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceUnit;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        if (quantity != null && priceUnit != null) {
            BigDecimal total = priceUnit.multiply(BigDecimal.valueOf(quantity));
            this.subtotal = total.subtract(discount != null ? discount : BigDecimal.ZERO);
        }
    }
}
