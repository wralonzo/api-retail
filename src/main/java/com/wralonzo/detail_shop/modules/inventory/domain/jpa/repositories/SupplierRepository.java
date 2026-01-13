package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Supplier;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.projections.SupplierProjection;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
  Optional<Supplier> findByName(String name);

  Optional<Supplier> findByCode(String code);

  Page<SupplierProjection> findAllProjectedBy(Pageable pageable);
}
