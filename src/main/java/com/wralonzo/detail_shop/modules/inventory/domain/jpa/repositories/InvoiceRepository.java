package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findBySupplierId(Long supplierId, Pageable pageable);

    Page<Invoice> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.dueDate < :now")
    List<Invoice> findOverdueInvoices(@Param("status") Invoice.InvoiceStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
