package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(String name);
}
