package com.wralonzo.detail_shop.modules.organization.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.BranchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

  private final BranchRepository branchRepository;

  public Branch findById(Long id) {
    return branchRepository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("El recurso no existe"));
  }

  public List<Branch> getBranchesByCompany(Long idCompany) {
    return branchRepository.findByCompanyId(idCompany);
  }

}
