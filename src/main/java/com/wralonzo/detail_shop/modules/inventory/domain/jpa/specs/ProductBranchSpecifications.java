package com.wralonzo.detail_shop.modules.inventory.domain.jpa.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchConfig;

public class ProductBranchSpecifications {
  public static Specification<ProductBranchConfig> byBranchAndCategory(Long branchId, Long categoryId) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("branchId"), branchId));
      if (categoryId != null) {
        predicates.add(cb.equal(root.get("category").get("id"), categoryId));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
