package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.application.projections.RoleProjection;
import com.wralonzo.detail_shop.domain.entities.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Set<Role>> findAllByNameIn(Collection<String> names);

    Page<RoleProjection> findAllProjectedBy(Pageable pagable);
}
