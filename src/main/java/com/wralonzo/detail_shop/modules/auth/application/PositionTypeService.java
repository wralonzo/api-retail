package com.wralonzo.detail_shop.modules.auth.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.auth.domain.dtos.positiontype.PositionTypeRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.PositionTypeProjection;
import lombok.RequiredArgsConstructor;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PositionTypeRepository;

@Service
@RequiredArgsConstructor
public class PositionTypeService {
  private final PositionTypeRepository positionTypeRepository;

  public Page<PositionTypeProjection> getAll(Pageable pageable) {
    return positionTypeRepository.findAllProjectedBy(pageable);
  }

  public PositionType findById(long id) {
    return positionTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de posición no encontrado con ID: " + id));
  }

  @Transactional
  public PositionType create(PositionTypeRequest request) {
    PositionType positionType = PositionType.builder()
        .name(request.getName())
        .level(request.getLevel())
        .build();

    return positionTypeRepository.save(positionType);
  }

  @Transactional
  public PositionType update(Long id, PositionTypeRequest request) {
    PositionType positionType = findById(id);

    positionType.setName(request.getName());
    positionType.setLevel(request.getLevel());

    return positionTypeRepository.save(positionType);
  }

  @Transactional
  public void delete(Long id) {
    PositionType positionType = findById(id);
    positionTypeRepository.delete(positionType);
  }
}
