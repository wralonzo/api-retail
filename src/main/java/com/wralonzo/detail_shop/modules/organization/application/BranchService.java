package com.wralonzo.detail_shop.modules.organization.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.organization.domain.dtos.branch.BranchRequest;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Company;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.BranchRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.CompanyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

  private final BranchRepository branchRepository;
  private final CompanyRepository companyRepository;

  public Page<Branch> getAll(Pageable pageable) {
    Page<Branch> branchs = branchRepository.findAll(pageable);
    return branchs.map(branch -> Branch.builder()
        .id(branch.getId())
        .name(branch.getName())
        .code(branch.getCode())
        .address(branch.getAddress())
        .company(branch.getCompany())
        .build());
  }

  public Branch findById(Long id) {
    Branch branch = branchRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));
    return Branch.builder()
        .id(branch.getId())
        .name(branch.getName())
        .code(branch.getCode())
        .address(branch.getAddress())
        .company(branch.getCompany())
        .build();
  }

  public List<Branch> getBranchesByCompany(Long idCompany) {
    List<Branch> branchs = branchRepository.findByCompanyId(idCompany);
    return branchs.stream().map(branch -> Branch.builder()
        .id(branch.getId())
        .name(branch.getName())
        .code(branch.getCode())
        .address(branch.getAddress())
        .company(branch.getCompany())
        .build()).toList();
  }

  @Transactional
  public Branch create(BranchRequest request) {
    // Verificar que la compañía existe
    Company company = companyRepository.findById(request.getCompanyId())
        .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con ID: " + request.getCompanyId()));

    Branch branch = Branch.builder()
        .name(request.getName())
        .code(request.getCode())
        .address(request.getAddress())
        .company(company)
        .build();

    return branchRepository.save(branch);
  }

  @Transactional
  public Branch update(Long id, BranchRequest request) {
    Branch branch = findById(id);

    // Verificar que la compañía existe si se está cambiando
    if (request.getCompanyId() != null && !request.getCompanyId().equals(branch.getCompany().getId())) {
      Company company = companyRepository.findById(request.getCompanyId())
          .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con ID: " + request.getCompanyId()));
      branch.setCompany(company);
    }

    branch.setName(request.getName());
    branch.setCode(request.getCode());
    branch.setAddress(request.getAddress());

    return branchRepository.save(branch);
  }

  @Transactional
  public void delete(Long id) {
    Branch branch = findById(id);
    branchRepository.delete(branch);
  }
}
