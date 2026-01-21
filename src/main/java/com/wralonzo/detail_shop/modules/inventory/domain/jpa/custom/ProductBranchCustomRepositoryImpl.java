package com.wralonzo.detail_shop.modules.inventory.domain.jpa.custom;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ProductBranchCustomRepositoryImpl implements ProductBranchCustomRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional
  public int bulkUpdatePrice(Long branchId, Long categoryId, Double factor) {
    String jpql = "UPDATE ProductBranchPrice pbp " +
        "SET pbp.price = ROUND(pbp.price * :factor, 2) " +
        "WHERE pbp.branchConfig.id IN (" +
        "   SELECT pbc.id FROM ProductBranchConfig pbc " +
        "   WHERE pbc.branchId = :branchId " +
        "   AND pbc.category.id = :categoryId " +
        ")";

    return entityManager.createQuery(jpql)
        .setParameter("factor", factor)
        .setParameter("branchId", branchId)
        .setParameter("categoryId", categoryId)
        .executeUpdate();
  }
}