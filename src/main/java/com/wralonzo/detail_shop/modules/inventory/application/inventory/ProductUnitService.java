package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnit;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final WarehouseService warehouseService;

    public List<ProductUnit> getAllProductUnits() {
        UserBusinessContext context = warehouseService.getUserBusinessContext();
        List<ProductUnit> productUnits = productUnitRepository.findByIdBranch(context.branchId());
        return productUnits.stream().map(productUnit -> ProductUnit.builder()
                .id(productUnit.getId())
                .name(productUnit.getName())
                .conversionFactor(productUnit.getConversionFactor())
                .barcode(productUnit.getBarcode())
                .isBase(productUnit.isBase())
                .idBranch(productUnit.getIdBranch())
                .build()).toList();
    }

    public ProductUnit getProductUnitById(Long id) {
        ProductUnit productUnit = productUnitRepository.findById(id).orElse(null);
        if (productUnit != null) {
            UserBusinessContext context = warehouseService.getUserBusinessContext();
            if (productUnit.getIdBranch() != context.branchId()) {
                return null;
            }
            return ProductUnit.builder()
                    .id(productUnit.getId())
                    .name(productUnit.getName())
                    .conversionFactor(productUnit.getConversionFactor())
                    .barcode(productUnit.getBarcode())
                    .isBase(productUnit.isBase())
                    .idBranch(productUnit.getIdBranch())
                    .build();
        }
        return null;
    }

    public ProductUnit createProductUnit(ProductUnit productUnit) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();
        productUnit.setIdBranch(context.branchId());
        return productUnitRepository.save(productUnit);
    }

    public ProductUnit updateProductUnit(Long id, ProductUnit productUnit) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();
        ProductUnit existingProductUnit = productUnitRepository.findById(id).orElse(null);
        if (existingProductUnit != null) {
            existingProductUnit.setName(productUnit.getName());
            existingProductUnit.setConversionFactor(productUnit.getConversionFactor());
            existingProductUnit.setBarcode(productUnit.getBarcode());
            existingProductUnit.setBase(productUnit.isBase());
            existingProductUnit.setIdBranch(context.branchId());
            return productUnitRepository.save(existingProductUnit);
        }
        return null;
    }

    public void deleteProductUnit(Long id) {
        productUnitRepository.deleteById(id);
    }

}
