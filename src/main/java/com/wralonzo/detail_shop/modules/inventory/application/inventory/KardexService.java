package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.KardexResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryMovement;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryMovementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexService {

  private final InventoryMovementRepository inventoryMovementRepository;

  @Transactional(readOnly = true)
  public List<KardexResponse> getKardex(Long productId, Long warehouseId, LocalDate startDate, LocalDate endDate) {

    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atTime(23, 59, 59);

    List<InventoryMovement> movements = inventoryMovementRepository
        .findByProductIdAndWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(productId, warehouseId, start, end);

    return movements.stream()
        .map((InventoryMovement m) -> KardexResponse.builder()
            .date(m.getCreatedAt())
            .movementType(m.getMovementType())
            .quantity(m.getQuantity())
            .previousStock(m.getPreviousStock())
            .currentStock(m.getCurrentStock())
            .reference(m.getReference())
            .build())
        .collect(Collectors.toList());
  }

}
