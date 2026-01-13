package com.wralonzo.detail_shop.domain.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // Stock y Estado
    private Integer stockMinim;
    private Boolean active;

    // Datos de Relaciones (Aplanados)
    private Long categoryId;
    private String categoryName;

    private Long supplierId;
    private String supplierName;

    private Long warehouseId;
    private String warehouseName;
    private BigDecimal pricePurchase;

    // Auditoría (Opcional, útil para el frontend)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}