package com.wralonzo.detail_shop.modules.auth.domain.mapper.records;

import java.util.List;

// Bloque "user"
public record UserBaseResponse(
        Long id,
        boolean enabled,
        String provider,
        List<String> roles,
        String token) {
}
