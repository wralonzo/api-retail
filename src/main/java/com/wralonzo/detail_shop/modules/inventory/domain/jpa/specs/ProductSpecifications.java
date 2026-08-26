package com.wralonzo.detail_shop.modules.inventory.domain.jpa.specs;

import org.springframework.data.jpa.domain.Specification;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;

public class ProductSpecifications {

  public static Specification<Product> searchByTerm(String term) {
    return (root, query, cb) -> {
      if (term == null || term.trim().isEmpty()) {
        return cb.conjunction();
      }

      String pattern = "%" + term.toLowerCase() + "%";

      return cb.or(
          cb.like(cb.lower(root.get("name")), pattern),
          cb.like(cb.lower(root.get("sku")), pattern),
          cb.like(cb.lower(root.get("barcode")), pattern));
    };
  }

  public static Specification<Product> isActive(Boolean active) {
    return (root, query, cb) -> (active == null) ? cb.conjunction() : cb.equal(root.get("active"), active);
  }

  public static Specification<Product> isService(Boolean isService) {
    return (root, query, cb) -> (isService == null) ? cb.conjunction() : cb.equal(root.get("isService"), isService);
  }

  public static Specification<Product> hasCompany(Long companyId) {
    return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
  }
}