package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnitDetails;

import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductUnitDetailsRepository extends JpaRepository<ProductUnitDetails, Long> {
    List<ProductUnitDetails> findByProductId(Long productId);

    @Transactional
    int deleteByProductIdAndUnitProductId(Long productId, Long unitProductId);
}
