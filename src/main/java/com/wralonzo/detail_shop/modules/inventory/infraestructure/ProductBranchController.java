package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.modules.inventory.application.product.ProductBranchService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBranchConfigDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/branches-config")
@RequiredArgsConstructor
public class ProductBranchController {

    private final ProductBranchService productBranchService;

    @PostMapping
    public ResponseEntity<ProductBranchConfigDto> create(
            @PathVariable Long idProduct,
            @Valid @RequestBody ProductBranchConfigDto dto) {
        return ResponseEntity.ok(productBranchService.create(idProduct, dto));
    }

    @PutMapping("/{idProductBranchConfig}")
    public ResponseEntity<ProductBranchConfigDto> update(
            @PathVariable Long idProduct,
            @PathVariable Long idProductBranchConfig,
            @Valid @RequestBody ProductBranchConfigDto dto) {
        return ResponseEntity.ok(productBranchService.update(idProduct, idProductBranchConfig, dto));
    }

    @GetMapping("/{idProductBranchConfig}")
    public ResponseEntity<ProductBranchConfigDto> getById(@PathVariable Long idProductBranchConfig) {
        return ResponseEntity.ok(productBranchService.getOneRecord(idProductBranchConfig));
    }

    @DeleteMapping("/{idProductBranchConfig}")
    public ResponseEntity<Void> delete(@PathVariable Long idProductBranchConfig) {
        productBranchService.delete(idProductBranchConfig);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{idProduct}")
    public ResponseEntity<List<ProductBranchConfigDto>> getAll(@PathVariable Long idProduct) {
        return ResponseEntity.ok(productBranchService.getAll(idProduct));
    }
}
