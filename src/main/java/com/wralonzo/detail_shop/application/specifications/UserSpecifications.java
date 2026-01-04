package com.wralonzo.detail_shop.application.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

// IMPORTANTE: Asegúrate de importar TU entidad Role
import com.wralonzo.detail_shop.domain.entities.Role;
import com.wralonzo.detail_shop.domain.entities.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

  public static Specification<User> filterUsers(String term, String roleName) {
    return (root, query, cb) -> {
      // 1. Evitar duplicados si hay Joins con colecciones
      query.distinct(true);

      List<Predicate> predicates = new ArrayList<>();

      // 2. Filtro: u.client IS NULL
      predicates.add(cb.isNull(root.get("client")));

      // 3. Filtro: roleName (Join con roles)
      if (roleName != null && !roleName.isEmpty()) {
        // Especificamos que se una a nuestra entidad Role
        Join<User, Role> rolesJoin = root.join("roles");
        predicates.add(cb.equal(rolesJoin.get("name"), roleName));
      }

      // 4. Filtro: term (username o fullName)
      if (term != null && !term.trim().isEmpty()) {
        String pattern = "%" + term.toLowerCase() + "%";

        Predicate usernameLike = cb.like(cb.lower(root.get("username")), pattern);
        Predicate fullNameLike = cb.like(cb.lower(root.get("fullName")), pattern);

        predicates.add(cb.or(usernameLike, fullNameLike));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}