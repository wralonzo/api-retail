package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.application.repositories.PositionTypeRepository;
import com.wralonzo.detail_shop.domain.entities.PositionType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PosiontTypeService {
    private final PositionTypeRepository positionTypeRepository;

    public List<PositionTypeProjection> getAll(){
        return this.positionTypeRepository.findAllProjectedBy();
    }
}
