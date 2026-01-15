package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // Buscar productos donde la cantidad sea menor o igual a la alerta definida
/*     @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId " +
            "AND i.quantity <= i.alertQuantity AND i.deletedAt IS NULL") */
    List<Inventory> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    // Verifica si ya existe una ficha de inventario para ese producto en ese almacén
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);
}