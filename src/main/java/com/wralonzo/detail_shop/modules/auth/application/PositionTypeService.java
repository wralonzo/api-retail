package com.wralonzo.detail_shop.modules.auth.application;

import org.springframework.stereotype.Service;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import lombok.RequiredArgsConstructor;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PositionTypeRepository;

@Service
@RequiredArgsConstructor
public class PositionTypeService {
  private final PositionTypeRepository positionTypeRepository;

  public PositionType findById(long id) {
    return positionTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("Recurso no encontrado"));
  }
}
