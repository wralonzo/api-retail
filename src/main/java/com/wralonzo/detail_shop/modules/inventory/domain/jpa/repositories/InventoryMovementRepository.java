package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryMovement;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProductIdAndWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long productId,
            Long warehouseId,
            LocalDateTime start,
            LocalDateTime end);
}
