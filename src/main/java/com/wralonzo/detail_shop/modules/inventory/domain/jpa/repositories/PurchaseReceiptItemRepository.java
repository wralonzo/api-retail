package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.PurchaseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseReceiptItemRepository extends JpaRepository<PurchaseReceiptItem, Long> {

    List<PurchaseReceiptItem> findByPurchaseReceiptId(Long purchaseReceiptId);

    List<PurchaseReceiptItem> findByProductId(Long productId);
}
