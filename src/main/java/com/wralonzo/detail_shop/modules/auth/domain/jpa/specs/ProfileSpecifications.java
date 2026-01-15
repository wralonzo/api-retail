package com.wralonzo.detail_shop.modules.auth.domain.jpa.specs;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public class ProfileSpecifications {

    public static Specification<Profile> hasUserIdIn(List<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty())
                return cb.disjunction();

            // Unimos con User para filtrar por su ID
            Join<Profile, User> userJoin = root.join("user");
            return userJoin.get("id").in(userIds);
        };
    }

    public static Specification<Profile> containsTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank())
                return null;

            String likeTerm = "%" + term.toLowerCase() + "%";

            // Filtros: fullName OR email OR phone
            Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), likeTerm);
            Predicate emailPredicate = cb.like(cb.lower(root.get("email")), likeTerm);
            Predicate phonePredicate = cb.like(root.get("phone"), likeTerm);

            return cb.or(namePredicate, emailPredicate, phonePredicate);
        };
    }

    public static Specification<Profile> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}