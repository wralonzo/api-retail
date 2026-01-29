package com.wralonzo.detail_shop.modules.auth.infraestructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.auth.application.RoleService;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.role.AssignPermissionsRequest;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.role.RoleRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.RoleProjection;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("role")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @GetMapping()
  public ResponseEntity<Page<RoleProjection>> getAll(
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    Page<RoleProjection> roles = this.roleService.find(pageable);
    return ResponseUtil.ok(roles);
  }

  @PostMapping()
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Role> create(@Valid @RequestBody RoleRequest request) {
    Role role = this.roleService.create(request);
    return ResponseUtil.ok(role);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Role> getById(@PathVariable Long id) {
    Role role = this.roleService.getById(id);
    return ResponseUtil.ok(role);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Role> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
    Role role = this.roleService.update(id, request);
    return ResponseUtil.ok(role);
  }

  @PatchMapping("/{id}/delete")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    this.roleService.delete(id);
    return ResponseUtil.ok(Map.of("message", "Rol eliminado exitosamente"));
  }

  @PostMapping("/{id}/permissions")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Role> assignPermissions(
      @PathVariable Long id,
      @Valid @RequestBody AssignPermissionsRequest request) {
    Role role = this.roleService.assignPermissions(id, request);
    return ResponseUtil.ok(role);
  }

  @DeleteMapping("/{id}/permissions")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Role> removePermissions(
      @PathVariable Long id,
      @Valid @RequestBody AssignPermissionsRequest request) {
    Role role = this.roleService.removePermissions(id, request);
    return ResponseUtil.ok(role);
  }
}
