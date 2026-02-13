package com.wralonzo.detail_shop.modules.inventory.domain.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBranchConfigDto {
    private Long id;
    private Long branchId;
    private Long categoryId;
    private String categoryName;
    private Boolean active;
    private Integer stockMinim;
}
