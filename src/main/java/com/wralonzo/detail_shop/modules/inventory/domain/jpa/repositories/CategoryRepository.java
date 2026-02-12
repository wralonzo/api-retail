package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByName(String name);

  Optional<Category> findByCode(String code);

  // Listar solo categorías de una sucursal específica que no estén borradas
  Page<Category> findByBranchIdAndDeletedAtIsNull(Long branchId, Pageable pageable);

  // Buscar por nombre dentro de una sucursal (para evitar duplicados locales)
  Optional<Category> findByNameAndBranchIdAndDeletedAtIsNull(String name, Long branchId);

  // Listar categorías para SuperAdmin (todas las de una lista de IDs de
  // almacenes)
  List<Category> findByBranchIdInAndDeletedAtIsNull(List<Long> branchid);

  boolean existsByIdAndBranchId(Long categoryId, Long branchId);

  // List<Category> findByCompanyId(Long companyId);
}
