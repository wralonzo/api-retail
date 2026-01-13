package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // Buscar todos los detalles de un pedido por el ID de la orden
    List<OrderDetail> findByOrderId(Long orderId);

    // Buscar todos los detalles que contengan un producto específico
    List<OrderDetail> findByProductId(Long productId);

    // Buscar por cantidad mínima
    List<OrderDetail> findByQuantityGreaterThanEqual(Integer quantity);
}
