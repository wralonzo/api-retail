package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.ProductService;
import com.wralonzo.detail_shop.domain.dto.product.ProductRequest;
import com.wralonzo.detail_shop.domain.dto.product.ProductUpdateRequest;
import com.wralonzo.detail_shop.domain.entities.Product;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping()
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest productRequest){
        Product product = this.productService.create(productRequest);
        return ResponseUtil.created(product, product.getId());
    }

    @GetMapping()
    public ResponseEntity<List<Product>> getAll(){
        return  ResponseEntity.ok(this.productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id){
        return ResponseEntity.ok(this.productService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> update(
            @Valid @RequestBody ProductUpdateRequest payload) {
        return ResponseEntity.ok(productService.update(payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/stock/{id}")
    public ResponseEntity<Product> updateStock(
            @Valid @RequestBody ProductUpdateRequest payload) {
        return ResponseEntity.ok(productService.updateStock(payload));
    }
}
