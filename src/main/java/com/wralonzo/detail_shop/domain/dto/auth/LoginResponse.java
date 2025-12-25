package com.wralonzo.detail_shop.domain.dto.auth;

import com.wralonzo.detail_shop.domain.entities.User;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private LocalDateTime deletedAt;
    private List<String> roles;

}