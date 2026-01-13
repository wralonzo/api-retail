package com.wralonzo.detail_shop.modules.organization.application;

import org.springframework.stereotype.Service;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.CompanyRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Company;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;

@Service
@AllArgsConstructor
@Builder
public class CompanyService {
  private final CompanyRepository companyRespository;

  public Company getById(Long id) {
    return companyRespository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("La unidad de negocio no existeF"));
  }
}
