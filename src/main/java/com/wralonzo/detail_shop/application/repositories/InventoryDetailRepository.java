package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.InventoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

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
