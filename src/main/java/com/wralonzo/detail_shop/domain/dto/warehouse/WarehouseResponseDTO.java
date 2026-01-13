package com.wralonzo.detail_shop.domain.dto.warehouse;

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