package com.wralonzo.detail_shop.modules.organization.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.BranchRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.CompanyRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Company;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User; // Asegura este import
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyService {
  private final CompanyRepository companyRespository;
  private final WarehouseService warehouseService;
  private final BranchRepository branchRepository;
  private final UserRepository userRepository;

  public Company getById(Long id) {
    return companyRespository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("La unidad de negocio no existe"));
  }

  /**
   * Método público para que otros servicios (como ClientService)
   * obtengan la compañía del usuario en sesión.
   */
  @Transactional(readOnly = true)
  public Long getCurrentUserCompanyId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      throw new ResourceUnauthorizedException("Sesión no válida o expirada");
    }

    if (authentication.getPrincipal() instanceof User userPrincipal) {
      // RE-CONSULTA al usuario usando su ID para que esté dentro de la sesión de
      // Hibernate
      User user = userRepository.findById(userPrincipal.getId())
          .orElseThrow(() -> new ResourceUnauthorizedException("Usuario no encontrado"));

      if (user.getEmployee() == null) {
        throw new ResourceConflictException("El usuario no tiene un almacén asignado.");
      }

      // Al estar dentro de @Transactional y ser un objeto gestionado,
      // Hibernate cargará el empleado sin errores.
      Warehouse warehouse = warehouseService.getById(user.getEmployee().getWarehouseId());

      return branchRepository.findById(warehouse.getBranch().getId())
          .map(branch -> branch.getCompany().getId())
          .orElseThrow(() -> new ResourceConflictException("Error en jerarquía de compañía"));
    }

    throw new ResourceUnauthorizedException("No se pudo determinar la unidad de negocio del usuario");
  }
}