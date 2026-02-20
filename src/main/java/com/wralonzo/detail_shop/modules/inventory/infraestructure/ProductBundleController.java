package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.modules.inventory.application.product.ProductBundleService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBundleDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class ProductBundleController {

    private final ProductBundleService productBundleService;

    @PostMapping
    public ResponseEntity<ProductBundleDto> create(
            @PathVariable Long idProduct,
            @Valid @RequestBody ProductBundleDto dto) {
        return ResponseEntity.ok(productBundleService.create(idProduct, dto));
    }

    @PutMapping("/{idProductBundle}")
    public ResponseEntity<ProductBundleDto> update(
            @PathVariable Long idProduct,
            @PathVariable Long idProductBundle,
            @Valid @RequestBody ProductBundleDto dto) {
        return ResponseEntity.ok(productBundleService.update(idProduct, idProductBundle, dto));
    }

    @DeleteMapping("/{idProductBundle}")
    public ResponseEntity<Void> delete(@PathVariable Long idProductBundle) {
        productBundleService.delete(idProductBundle);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idProductBundle}")
    public ResponseEntity<ProductBundleDto> getOneRecord(@PathVariable Long idProductBundle) {
        return ResponseEntity.ok(productBundleService.getOneRecord(idProductBundle));
    }

    @GetMapping("/products/{idProduct}")
    public ResponseEntity<List<ProductBundleDto>> getAll(@PathVariable Long idProduct) {
        return ResponseEntity.ok(productBundleService.getAll(idProduct));
    }
}
