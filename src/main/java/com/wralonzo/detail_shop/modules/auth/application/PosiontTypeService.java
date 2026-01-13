package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PositionTypeRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PosiontTypeService {
    private final PositionTypeRepository positionTypeRepository;

    public Page<PositionTypeProjection> getAll(Pageable pageable) {
        return this.positionTypeRepository.findAllProjectedBy(pageable);
    }
}
