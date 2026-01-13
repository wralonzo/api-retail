package com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<WarehouseProjection> findAllProjectedBy();

    Optional<Warehouse> findByCode(String code);

}
