package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImportReport {
  private int created;
  private int updated;
  private int failed;
  private List<RowError> details;
  private byte[] excelReport; // El archivo con los errores marcados
}