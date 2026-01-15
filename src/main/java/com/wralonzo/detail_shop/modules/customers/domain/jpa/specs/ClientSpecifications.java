package com.wralonzo.detail_shop.modules.customers.domain.jpa.specs;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;

public class ClientSpecifications {

  public static Specification<Client> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Client> hasClientType(ClientType clientType) {
    return (root, query, cb) -> clientType == null
        ? cb.conjunction()
        : cb.equal(root.get("clientType"), clientType);
  }

  public static Specification<Client> containsTerm(String term, List<Long> profileIdsFromAuth) {
    return (root, query, cb) -> {
      if (term == null || term.trim().isEmpty()) {
        return cb.conjunction();
      }

      String pattern = "%" + term.toLowerCase() + "%";

      // Predicados de búsqueda local (Customers)
      var localPredicate = cb.or(
          cb.like(cb.lower(root.get("code")), pattern),
          cb.like(cb.lower(root.get("taxId")), pattern));

      // Si el módulo Auth encontró coincidencias en identidades

      // TIP: Si también tienes profileIds de Auth, añádelos aquí:
      if (profileIdsFromAuth != null && !profileIdsFromAuth.isEmpty()) {
        localPredicate = cb.or(localPredicate,
            root.get("profileId").in(profileIdsFromAuth));
      }

      return localPredicate;
    };
  }

  public static Specification<Client> hasCompanyId(Long companyId) {
    return (root, query, cb) -> {
      if (companyId == null)
        return null;
      return cb.equal(root.get("companyId"), companyId);
    };
  }
}