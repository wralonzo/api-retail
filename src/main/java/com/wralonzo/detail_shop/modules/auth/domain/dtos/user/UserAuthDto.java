package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthDto {
  private Long id;
  private String username;
  private String fullName;
  private String email;
  private String phone;
  private String passwordInit;
  private List<String> roles;
  private String avatar;
}