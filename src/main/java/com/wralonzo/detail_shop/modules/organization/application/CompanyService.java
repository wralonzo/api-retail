package com.wralonzo.detail_shop.modules.organization.application;

import java.util.Optional;

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
  public Optional<Long> getCurrentUserCompanyId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 1. Validación de seguridad básica
    if (authentication == null || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof User userPrincipal)) {
      throw new ResourceUnauthorizedException("Sesión no válida o expirada");
    }

    // 2. Bypass para SUPER_ADMIN
    // Si es el "Boss", devolvemos Optional.empty() indicando que NO hay filtro de
    // compañía
    boolean isSuperAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    if (isSuperAdmin) {
      return Optional.empty();
    }

    // 3. Carga eficiente para roles normales (Admin, Vendedor, Bodega)
    // Usamos el método con EntityGraph que configuramos al inicio para traer
    // empleado y warehouse de un solo golpe
    User user = userRepository.findById(userPrincipal.getId())
        .orElseThrow(() -> new ResourceUnauthorizedException("Usuario no encontrado"));

    // 4. Validaciones de integridad de negocio
    if (user.getEmployee() == null || user.getEmployee().getWarehouseId() == null) {
      throw new ResourceConflictException("El usuario debe estar vinculado a un empleado y almacén.");
    }

    // 5. Navegación en la jerarquía
    Warehouse warehouse = warehouseService.getById(user.getEmployee().getWarehouseId());

    Long companyId = branchRepository.findById(warehouse.getBranch().getId())
        .map(branch -> branch.getCompany().getId())
        .orElseThrow(() -> new ResourceConflictException("Error en jerarquía de compañía"));

    return Optional.of(companyId);
  }
}