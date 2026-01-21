package com.wralonzo.detail_shop.modules.inventory.domain.dtos.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {
  @NotEmpty(message = "El nombre no debe estar vacío")
  @Size(min = 4, max = 50)
  @NotBlank()
  private String name;

  @NotEmpty(message = "El código no debe estar vacío")
  @Size(min = 2, max = 50)
  @NotBlank()
  private String code;

  private String notes;

  @NotNull()
  @Positive()
  private Long branchId;
}