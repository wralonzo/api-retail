package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {
}
