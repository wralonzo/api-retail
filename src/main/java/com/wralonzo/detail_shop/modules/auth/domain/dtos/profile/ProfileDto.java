package com.wralonzo.detail_shop.modules.auth.domain.dtos.profile;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDto {

  private String fullName;

  private String phone;

  private String address;

  private String avatar;

  private LocalDateTime createdAt;

  private LocalDateTime updateAt;

  private LocalDateTime deletedAt;
}
