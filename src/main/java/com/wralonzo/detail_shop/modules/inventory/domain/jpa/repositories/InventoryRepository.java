package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    @EntityGraph(attributePaths = { "product", "product.category" })
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // Buscar productos donde la cantidad sea menor o igual a la alerta definida
    /*
     * @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId " +
     * "AND i.quantity <= i.alertQuantity AND i.deletedAt IS NULL")
     */
    List<Inventory> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    // Para ver el stock global de la compañía (Varios almacenes)
    List<Inventory> findByWarehouseIdIn(List<Long> warehouseIds);

    // Verifica si ya existe una ficha de inventario para ese producto en ese
    // almacén
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("SELECT i FROM Inventory i " +
            "JOIN FETCH i.product p " +
            "WHERE i.warehouseId IN :warehouseIds " +
            "AND i.quantity <= i.alertQuantity " +
            "AND i.deletedAt IS NULL")
    List<Inventory> findLowStockByWarehouseIds(@Param("warehouseIds") List<Long> warehouseIds);
}