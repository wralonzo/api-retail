package com.wralonzo.detail_shop.modules.auth.domain.dtos.auth;

import lombok.Builder;
import lombok.Data;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private UserShortResponse user;
    private String token;
}