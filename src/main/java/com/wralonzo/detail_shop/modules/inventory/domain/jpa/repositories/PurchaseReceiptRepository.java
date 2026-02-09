package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.PurchaseReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {

    Optional<PurchaseReceipt> findByPurchaseNumber(String purchaseNumber);

    Page<PurchaseReceipt> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseReceipt> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<PurchaseReceipt> findByInvoiceId(Long invoiceId, Pageable pageable);

    @Query("SELECT pr FROM PurchaseReceipt pr WHERE pr.receiptDate BETWEEN :startDate AND :endDate")
    Page<PurchaseReceipt> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
