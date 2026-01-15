package com.wralonzo.detail_shop.modules.auth.domain.dtos.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
  private Long id;
  private Long warehouseId;
  private String positionTypeName; // Nombre del cargo/puesto
  private Long positionTypeId;
}