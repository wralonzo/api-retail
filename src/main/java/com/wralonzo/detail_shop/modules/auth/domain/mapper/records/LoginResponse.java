package com.wralonzo.detail_shop.modules.auth.domain.mapper.records;

// El objeto raíz de la respuesta
public record LoginResponse(
                ProfileResponse profile,
                UserBaseResponse user,
                EmployeeShortResponse employee) {
}