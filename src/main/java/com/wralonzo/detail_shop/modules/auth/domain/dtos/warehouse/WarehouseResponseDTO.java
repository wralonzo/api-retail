package com.wralonzo.detail_shop.modules.auth.domain.dtos.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarehouseResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String boss;
}