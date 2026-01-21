package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal; // Recomendado para dinero
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", schema = "inventory", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "sku", "company_id" }) // Unicidad corporativa
}, indexes = {
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_product")
        private Long id;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(columnDefinition = "TEXT")
        private String description;

        @Column(name = "company_id", nullable = false)
        private Long companyId;

        @Column(nullable = false, unique = true, length = 50)
        private String sku;

        @Column(length = 50)
        private String barcode;

        // Cambiado a BigDecimal por precisión en cálculos financieros
        @Column(name = "price_purchase", precision = 12, scale = 2)
        private BigDecimal pricePurchase;

        @Column(name = "base_price", nullable = false)
        private BigDecimal basePrice;

        @Enumerated(EnumType.STRING)
        @Column(name = "product_type", nullable = false)
        @Builder.Default
        private ProductType type = ProductType.STANDARD;

        @Builder.Default
        @Column(nullable = false)
        private Boolean active = true;

        @Builder.Default
        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductUnit> units = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "comboProduct", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductBundle> bundleItems = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
        private List<ProductBranchConfig> branchConfigs = new ArrayList<>();

        // Colecciones inicializadas para evitar NullPointerException
        @Builder.Default
        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SaleDetail> saleDetails = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<OrderDetail> orderDetails = new ArrayList<>();

        // AUDITORÍA
        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "update_at")
        private LocalDateTime updateAt;

        @Column(name = "deleted_at")
        private LocalDateTime deletedAt;
}