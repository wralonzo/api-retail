package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barcode;

    // Precios
    private BigDecimal priceSale;
    private BigDecimal basePrice;
    private BigDecimal pricePurchase;

    // Stock y Estado
    private Integer stockMinim;
    private Boolean active;

    // Tipo y Distinción Producto vs Servicio
    private ProductType type;
    private Boolean isService;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}