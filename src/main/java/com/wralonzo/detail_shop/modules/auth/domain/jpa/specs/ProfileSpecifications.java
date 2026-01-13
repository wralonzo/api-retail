package com.wralonzo.detail_shop.modules.auth.domain.jpa.specs;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import jakarta.persistence.criteria.Join;
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
}