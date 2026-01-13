package com.wralonzo.detail_shop.domain.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.wralonzo.detail_shop.domain.enums.ProviderRegister;

@Data
@Builder
public class LoginResponse {
    private Integer id;
    private String token;
    private String fullName;
    private String username;
    private String phone;
    private String address;
    private String avatar;
    private String password;
    private String passwordInit;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private LocalDateTime deletedAt;
    private List<String> roles;
    private Long clientId;
    private ProviderRegister provider;
    private LocalDateTime lastLoginAt;
    private EmployeeShortResponse employee;

}