package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wralonzo.detail_shop.modules.inventory.application.product.ProductUnitListService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductUnitResponse;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("product/units/list")
@AllArgsConstructor
public class ProductUnitListController {

    private final ProductUnitListService productUnitListService;

    @GetMapping
    public ResponseEntity<List<ProductUnitResponse>> getAllProductUnits(@RequestParam Long productId) {
        return ResponseEntity.ok(productUnitListService.getAllProductUnits(productId));
    }

    @GetMapping("/add")
    public ResponseEntity<?> getAllProductUnitsToAdd(@RequestParam Long productId, @RequestParam Long idUnit) {
        productUnitListService.createProductUnit(productId, idUnit);
        return ResponseEntity.ok(productUnitListService.getAllProductUnits(productId));
    }

    @PatchMapping("/delete")
    public ResponseEntity<?> deleteProductUnit(@RequestParam Long id) {
        productUnitListService.deleteProductUnit(id);
        return ResponseEntity.ok(productUnitListService.getAllProductUnits(id));
    }
}
