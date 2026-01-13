package com.wralonzo.detail_shop.domain.dto.auth;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeShortResponse {
  private Long id;
  private Long warehouseId;
  private String warehouseName;

  // Datos del Puesto
  private Long positionId;
  private String positionName;
}