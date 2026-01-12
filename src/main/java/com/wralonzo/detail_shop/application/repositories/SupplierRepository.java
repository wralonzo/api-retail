package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.application.projections.SupplierProjection;
import com.wralonzo.detail_shop.domain.entities.Supplier;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
  Optional<Supplier> findByName(String name);

  Page<SupplierProjection> findAllProjectedBy(Pageable pageable);
}
