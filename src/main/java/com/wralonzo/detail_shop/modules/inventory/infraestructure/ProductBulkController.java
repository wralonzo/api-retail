package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.modules.inventory.application.product.ProductBulkService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.BulkPriceAdjustmentRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("product-bulk")
@RequiredArgsConstructor
public class ProductBulkController {
  private final ProductBulkService productBulkService;

  @PatchMapping("/price-adjustment")
  public ResponseEntity<?> bulkPriceUpdate(
      @RequestBody BulkPriceAdjustmentRequest request) {
    int updatedCount = productBulkService.bulkPriceAdjustment(
        request.getWarehouseId(),
        request.getCategoryId(),
        request.getPercentage());

    return ResponseEntity.ok(Map.of(
        "message", "Precios actualizados correctamente",
        "updatedProducts", updatedCount));
  }
}
