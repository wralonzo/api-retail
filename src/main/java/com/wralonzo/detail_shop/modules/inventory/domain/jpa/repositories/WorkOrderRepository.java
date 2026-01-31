package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

}
