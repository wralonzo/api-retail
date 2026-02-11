package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnitDetails;

@Repository
public interface ProductUnitDetailsRepository extends JpaRepository<ProductUnitDetails, Long> {
    List<ProductUnitDetails> findByIdProduct(Long idProduct);
}
