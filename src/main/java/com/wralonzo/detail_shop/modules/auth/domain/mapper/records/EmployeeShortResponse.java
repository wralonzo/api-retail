package com.wralonzo.detail_shop.modules.auth.domain.mapper.records;

// Bloque "employee"
public record EmployeeShortResponse(
        Long id,
        Long warehouseId,
        String positionName,
        Long positionId) {
}