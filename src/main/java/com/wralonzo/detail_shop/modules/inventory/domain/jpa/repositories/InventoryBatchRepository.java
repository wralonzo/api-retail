package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryBatch;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    @Query("SELECT b FROM InventoryBatch b WHERE b.product.id = :pid AND b.warehouseId = :wid AND b.currentQuantity > 0 ORDER BY b.expirationDate ASC")
    List<InventoryBatch> findAvailableBatchesOrderByExpiration(Long pid, Long wid);

    // Para FEFO: Obtener lotes con existencia, ordenados por vencimiento
    @Query("SELECT b FROM InventoryBatch b " +
            "WHERE b.product.id = :productId " +
            "AND b.warehouseId = :warehouseId " +
            "AND b.currentQuantity > 0 " +
            "ORDER BY b.expirationDate ASC")
    List<InventoryBatch> findAvailableBatchesFEFO(@Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId);
}
