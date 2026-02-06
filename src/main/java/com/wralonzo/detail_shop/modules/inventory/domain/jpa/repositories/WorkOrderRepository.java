package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrder;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
    Optional<WorkOrder> findByReferenceNumber(String referenceNumber);
}
