package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsBySku(String sku);

    // Búsqueda por nombre o SKU (ignora mayúsculas/minúsculas)
    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku, Pageable pageable);

    // Solo productos no eliminados
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    Page<Product> findAllActive(Pageable pageable);

    List<Product> findByActiveTrue();

    @Query("SELECT DISTINCT p FROM Product p JOIN p.branchConfigs bc WHERE bc.category.id = :categoryId AND p.active = true")
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    boolean existsBySkuAndActiveTrue(String sku);

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySkuAndCompanyId(String sku, Long companyId);
}
