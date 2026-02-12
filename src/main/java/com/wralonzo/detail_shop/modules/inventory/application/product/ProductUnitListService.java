package com.wralonzo.detail_shop.modules.inventory.application.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductUnitResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnit;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnitDetails;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitDetailsRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductUnitListService {
    private final ProductUnitDetailsRepository productUnitDetailsRepository;

    public List<ProductUnitResponse> getAllProductUnits(Long productId) {
        List<ProductUnitDetails> productUnits = productUnitDetailsRepository.findByProductId(productId);
        return productUnits.stream().map(productUnit -> {
            ProductUnitResponse response = new ProductUnitResponse();
            response.setId(productUnit.getId());
            response.setName(productUnit.getUnitProduct().getName());
            response.setConversionFactor(productUnit.getUnitProduct().getConversionFactor());
            response.setBarcode(productUnit.getUnitProduct().getBarcode());
            return response;
        }).toList();
    }

    public void createProductUnit(Long idProduct, Long idUnit) {
        ProductUnitDetails productUnit = new ProductUnitDetails();
        Product product = Product.builder().id(idProduct).build();
        ProductUnit unitProduct = ProductUnit.builder().id(idUnit).build();
        productUnit.setUnitProduct(unitProduct);
        productUnit.setProduct(product);
        productUnitDetailsRepository.save(productUnit);
    }

    @Transactional
    public void deleteProductUnit(Long id) {
        productUnitDetailsRepository.deleteById(id);
    }

}
