package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {
  boolean existsByEmail(String email);
  Optional<Profile> findByEmail(String email);
}
