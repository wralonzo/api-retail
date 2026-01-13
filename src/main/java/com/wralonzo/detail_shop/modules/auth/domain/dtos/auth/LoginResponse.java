package com.wralonzo.detail_shop.modules.auth.domain.dtos.auth;

import lombok.Builder;
import lombok.Data;

import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private UserShortResponse user;
    private String token;
    private Employee employee;
    private Warehouse warehouse;

}