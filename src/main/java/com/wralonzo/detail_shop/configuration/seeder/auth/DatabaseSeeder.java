package com.wralonzo.detail_shop.configuration.seeder.auth;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Permission;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PermissionRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Override
  @Transactional
  public void run(String... args) {
    // 1. Permisos de Clientes
    Permission readClients = createPermissionIfNotFound("CLIENT_READ");
    Permission writeClients = createPermissionIfNotFound("CLIENT_CREATE");
    Permission updateClients = createPermissionIfNotFound("CLIENT_UPDATE");
    Permission deleteClients = createPermissionIfNotFound("CLIENT_DELETE");

    // 2. Permisos de Usuario (CORREGIDOS los nombres)
    Permission userCreate = createPermissionIfNotFound("USER_CREATE");
    Permission userRead = createPermissionIfNotFound("USER_READ");
    Permission userProfile = createPermissionIfNotFound("USER_PROFILE");
    Permission userUpdate = createPermissionIfNotFound("USER_UPDATE"); // Antes era USER_READ
    Permission userDelete = createPermissionIfNotFound("USER_DELETE"); // Antes era USER_READ

    // 3. Permisos de Compañía y Organización
    Permission companyManage = createPermissionIfNotFound("COMPANY_MANAGE");
    Permission companyViewAll = createPermissionIfNotFound("COMPANY_VIEW_ALL");
    Permission viewOrg = createPermissionIfNotFound("ORG_VIEW");
    Permission manageOrg = createPermissionIfNotFound("ORG_MANAGE");

    // 4. Crear Roles y asignar permisos usando el helper seguro

    // SUPER_ADMIN
    createRoleIfNotFound("ROLE_SUPER_ADMIN", "Acceso total global",
        safeSet(readClients, writeClients, updateClients, deleteClients,
            viewOrg, manageOrg, userCreate, userRead, userProfile,
            userDelete, userUpdate, companyManage, companyViewAll));

    // ROLE_ADMIN
    createRoleIfNotFound("ROLE_ADMIN", "Admin de su propia unidad",
        safeSet(userCreate, userRead, companyManage, viewOrg));

    // ROLE_VENDEDOR
    createRoleIfNotFound("ROLE_VENDEDOR", "Rol para ventas",
        safeSet(readClients, updateClients, deleteClients, userProfile));

    // ROLE_BODEGA
    createRoleIfNotFound("ROLE_BODEGA", "Rol para inventarios",
        safeSet(userRead, userProfile, viewOrg));

    // ROLE_CLIENTE
    createRoleIfNotFound("ROLE_CLIENTE", "Rol para cliente final",
        safeSet(userProfile));
  }

  /**
   * Helper para crear un Set sin que falle por duplicados accidentales
   */
  private Set<Permission> safeSet(Permission... permissions) {
    Set<Permission> set = new HashSet<>();
    if (permissions != null) {
      Collections.addAll(set, permissions);
    }
    return set;
  }

  private Permission createPermissionIfNotFound(String name) {
    return permissionRepository.findByName(name)
        .orElseGet(() -> permissionRepository.save(
            Permission.builder().name(name).build()));
  }

  private void createRoleIfNotFound(String name, String note, Set<Permission> permissions) {
    roleRepository.findByName(name).ifPresentOrElse(
        role -> {
          role.setPermissions(permissions);
          roleRepository.save(role);
        },
        () -> {
          Role newRole = Role.builder()
              .name(name)
              .note(note)
              .permissions(permissions)
              .build();
          roleRepository.save(newRole);
        });
  }
}