package com.wralonzo.detail_shop.modules.organization.domain.dtos.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse {
  private Long id;
  private String name;
  private String code;
  private String phone;
  private boolean active;
  private String branchName; // Opcional, para mostrar contexto
}