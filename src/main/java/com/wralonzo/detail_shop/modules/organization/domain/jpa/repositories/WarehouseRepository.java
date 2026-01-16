package com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<WarehouseProjection> findAllProjectedBy();

    Optional<Warehouse> findByCode(String code);

    @Query("SELECT w.id FROM Warehouse w WHERE w.branch.company.id = :companyId")
    List<Long> findAllIdsByCompanyId(@Param("companyId") Long companyId);
}
