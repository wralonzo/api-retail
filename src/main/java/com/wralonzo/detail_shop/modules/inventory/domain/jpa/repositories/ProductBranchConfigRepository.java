package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.custom.ProductBranchCustomRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchConfig;

public interface ProductBranchConfigRepository
        extends JpaRepository<ProductBranchConfig, Long>, ProductBranchCustomRepository {

    boolean existsByProductIdAndBranchId(Long idProduct, Long idBranch);

    Optional<ProductBranchConfig> findByProductIdAndBranchId(Long productId, Long branchId);

}
