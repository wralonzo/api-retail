package com.wralonzo.detail_shop.infrastructure.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.application.projections.RoleProjection;
import com.wralonzo.detail_shop.application.services.RoleService;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("role")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @GetMapping()
  public ResponseEntity<Page<RoleProjection>> getAll(
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    Page<RoleProjection> clients = this.roleService.find(pageable);
    return ResponseUtil.ok(clients);
  }

}
