package com.wralonzo.detail_shop.modules.auth.domain.dtos.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
  private Long id;
  private String fullName;
  private String email;
  private String phone;
  private String address;
  private String avatar;
}