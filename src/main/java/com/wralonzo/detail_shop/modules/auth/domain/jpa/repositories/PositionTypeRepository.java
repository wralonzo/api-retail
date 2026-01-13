package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.PositionTypeProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionTypeRepository extends JpaRepository<PositionType, Long> {
    Page<PositionTypeProjection> findAllProjectedBy(Pageable pageable);
}
