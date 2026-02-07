package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.modules.inventory.application.inventory.ProductUnitService;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnit;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory/product-units")
@RequiredArgsConstructor
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    @GetMapping
    public List<ProductUnit> getAllProductUnits() {
        return productUnitService.getAllProductUnits();
    }

    @GetMapping("/{id}")
    public ProductUnit getProductUnitById(@PathVariable Long id) {
        return productUnitService.getProductUnitById(id);
    }

    @PostMapping
    public ProductUnit createProductUnit(@RequestBody ProductUnit productUnit) {
        return productUnitService.createProductUnit(productUnit);
    }

    @PatchMapping("/{id}")
    public ProductUnit updateProductUnit(@PathVariable Long id, @RequestBody ProductUnit productUnit) {
        return productUnitService.updateProductUnit(id, productUnit);
    }

    @DeleteMapping("/{id}")
    public void deleteProductUnit(@PathVariable Long id) {
        productUnitService.deleteProductUnit(id);
    }

}
