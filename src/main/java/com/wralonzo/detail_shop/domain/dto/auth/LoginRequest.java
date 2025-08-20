package com.wralonzo.detail_shop.domain.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}