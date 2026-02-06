package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.application.sale.SaleService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<Page<SaleResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(saleService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@RequestBody @Valid SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.createSale(request));
    }
}
