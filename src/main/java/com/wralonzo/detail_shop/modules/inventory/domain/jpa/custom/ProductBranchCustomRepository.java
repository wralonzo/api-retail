package com.wralonzo.detail_shop.modules.inventory.domain.jpa.custom;

public interface ProductBranchCustomRepository {
  int bulkUpdatePrice(Long branchId, Long categoryId, Double factor);
}
