package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnit;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;

    public List<ProductUnit> getAllProductUnits() {
        return productUnitRepository.findAll();
    }

    public ProductUnit getProductUnitById(Long id) {
        return productUnitRepository.findById(id).orElse(null);
    }

    public ProductUnit createProductUnit(ProductUnit productUnit) {
        return productUnitRepository.save(productUnit);
    }

    public ProductUnit updateProductUnit(Long id, ProductUnit productUnit) {
        ProductUnit existingProductUnit = productUnitRepository.findById(id).orElse(null);
        if (existingProductUnit != null) {
            existingProductUnit.setName(productUnit.getName());
            existingProductUnit.setConversionFactor(productUnit.getConversionFactor());
            existingProductUnit.setBarcode(productUnit.getBarcode());
            existingProductUnit.setBase(productUnit.isBase());
            return productUnitRepository.save(existingProductUnit);
        }
        return null;
    }

    public void deleteProductUnit(Long id) {
        productUnitRepository.deleteById(id);
    }

}
