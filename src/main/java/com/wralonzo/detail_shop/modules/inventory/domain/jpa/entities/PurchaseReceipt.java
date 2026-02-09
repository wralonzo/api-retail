package com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String purchaseNumber;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false)
    private LocalDateTime receiptDate;

    @Column
    private Integer totalItems;

    @Column(length = 500)
    private String observation;

    @Column
    private Long invoiceId; // Referencia opcional a Invoice

    @OneToMany(mappedBy = "purchaseReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PurchaseReceiptItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public void addItem(PurchaseReceiptItem item) {
        items.add(item);
        item.setPurchaseReceipt(this);
    }

    public void calculateTotalItems() {
        this.totalItems = items.stream()
                .mapToInt(PurchaseReceiptItem::getQuantity)
                .sum();
    }
}
