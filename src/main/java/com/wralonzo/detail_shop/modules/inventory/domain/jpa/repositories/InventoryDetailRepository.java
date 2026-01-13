package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryDetail;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryDetailRepository extends JpaRepository<InventoryDetail, Long> {
    List<InventoryDetail> findByProductIdAndWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long productId,
            Long warehouseId,
            LocalDateTime start,
            LocalDateTime end
    );
}
