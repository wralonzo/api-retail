package com.wralonzo.detail_shop.modules.auth.domain.jpa.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

  public static Specification<User> filterUsers(String term, String roleName) {
    return (root, query, cb) -> {
      // 1. Evitar duplicados si hay Joins con colecciones
      query.distinct(true);

      List<Predicate> predicates = new ArrayList<>();

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

  public static Specification<User> searchByTerm(String term) {
    return (root, query, cb) -> {
      if (term == null || term.isBlank())
        return cb.conjunction();

      String pattern = "%" + term.toLowerCase() + "%";

      // Hacemos el JOIN programático con Profile
      Join<User, Profile> profileJoin = root.join("profile");

      // WHERE (LOWER(username) LIKE %term% OR LOWER(profile.fullName) LIKE %term%)
      return cb.or(
          cb.like(cb.lower(root.get("username")), pattern),
          cb.like(cb.lower(profileJoin.get("fullName")), pattern));
    };
  }

  public static Specification<User> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }
}