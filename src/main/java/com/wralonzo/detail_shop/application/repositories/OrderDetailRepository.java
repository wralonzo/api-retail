package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
