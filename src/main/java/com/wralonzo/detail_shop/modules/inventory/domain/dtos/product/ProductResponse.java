package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    private ProductType type;
    private List<ProductUnitDto> units;
    private List<ProductBundleDto> bundleItems;

    // Auditoría (Opcional, útil para el frontend)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}