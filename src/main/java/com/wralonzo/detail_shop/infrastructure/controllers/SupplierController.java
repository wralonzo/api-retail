package com.wralonzo.detail_shop.infrastructure.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.application.projections.SupplierProjection;
import com.wralonzo.detail_shop.application.services.SupplierService;
import com.wralonzo.detail_shop.domain.entities.Supplier;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {
  private final SupplierService supplierService;

  @GetMapping()
  public ResponseEntity<Page<SupplierProjection>> getAll(@PageableDefault(size = 100, sort = "name") Pageable pageable) {
    Page<SupplierProjection> suppliers = this.supplierService.getAll(pageable);
    return ResponseUtil.ok(suppliers);
  }

  @PostMapping()
  public ResponseEntity<Supplier> create(
      @Valid @RequestBody Supplier request) {
    Supplier supplier = this.supplierService.create(request);
    return ResponseUtil.ok(supplier);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Supplier> findOne(@PathVariable Long id) {
    Supplier supplier = this.supplierService.findOne(id);
    return ResponseUtil.ok(supplier);
  }

  @PatchMapping("/{id}/delete")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> deactivate(@PathVariable Long id) {
    supplierService.delete(id);
    return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Supplier request) {
    supplierService.update(id, request);
    return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
  }
}
