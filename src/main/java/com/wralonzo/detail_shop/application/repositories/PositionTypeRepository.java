package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.domain.entities.PositionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionTypeRepository extends JpaRepository<PositionType, Long> {
    Page<PositionTypeProjection> findAllProjectedBy(Pageable pageable);
}
