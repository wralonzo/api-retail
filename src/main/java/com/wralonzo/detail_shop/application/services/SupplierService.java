package com.wralonzo.detail_shop.application.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.application.projections.SupplierProjection;
import com.wralonzo.detail_shop.application.repositories.SupplierRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.domain.entities.Supplier;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

  private final SupplierRepository supplierRepository;

  public Supplier create(Supplier request) {
    return this.supplierRepository.save(request);
  }

  public Page<SupplierProjection> getAll(Pageable pageable) {
    return this.supplierRepository.findAllProjectedBy(pageable);
  }

  public Supplier findOne(Long id) {
    return this.supplierRepository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("No se encontro el recurso"));
  }

  public Supplier update(Long id, Supplier request) {
    this.findOne(id);
    return this.supplierRepository.save(request);
  }

  public void delete(Long id) {
    Supplier supplier = this.findOne(id);
    supplier.setDeletedAt(LocalDateTime.now());
    this.supplierRepository.save(supplier);
  }

}
