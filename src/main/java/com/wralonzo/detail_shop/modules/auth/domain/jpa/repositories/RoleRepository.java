package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.RoleProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Set<Role>> findAllByNameIn(Collection<String> names);
    List<Role> findByNameIn(Collection<String> names);

    Page<RoleProjection> findAllProjectedBy(Pageable pagable);
}
