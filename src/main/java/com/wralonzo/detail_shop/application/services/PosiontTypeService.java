package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.application.repositories.PositionTypeRepository;
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
