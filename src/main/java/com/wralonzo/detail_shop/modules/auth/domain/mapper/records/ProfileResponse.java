package com.wralonzo.detail_shop.modules.auth.domain.mapper.records;

import java.time.LocalDate;

// Bloque "profile"
public record ProfileResponse(
        Long id,
        String username,
        String fullName,
        String provide, // provider
        String passwordInit,
        String avatar,
        String address,
        String phone,
        String email,
        LocalDate birthDate) {
}
