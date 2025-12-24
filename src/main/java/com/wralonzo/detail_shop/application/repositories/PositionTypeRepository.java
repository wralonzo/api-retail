package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.domain.entities.PositionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionTypeRepository extends JpaRepository<PositionType, Long> {
    List<PositionTypeProjection> findAllProjectedBy();
}
