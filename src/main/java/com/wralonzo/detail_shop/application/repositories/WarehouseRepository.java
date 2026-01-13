package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.application.projections.WarehouseProjection;
import com.wralonzo.detail_shop.domain.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<WarehouseProjection> findAllProjectedBy();
}
