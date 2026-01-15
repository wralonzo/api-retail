package com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {
  
}
