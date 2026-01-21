package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RowError {
  private int rowNum;
  private String sku;
  private String errorMessage;
}