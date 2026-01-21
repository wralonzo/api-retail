package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.modules.inventory.application.inventory.KardexService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.KardexResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-kardex")
@RequiredArgsConstructor
public class ProductKardexController {
  private final KardexService kardexService;

  @GetMapping("/kardex")
  public ResponseEntity<?> getKardex(
      @RequestParam Long productId,
      @RequestParam Long warehouseId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    List<KardexResponse> report = kardexService.getKardex(productId, warehouseId, startDate, endDate);

    if (report.isEmpty()) {
      return ResponseEntity.ok(Map.of());
    }

    return ResponseEntity.ok(report);
  }
}
