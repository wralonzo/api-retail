
package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import java.time.LocalDate;
import java.util.List;

import com.wralonzo.detail_shop.modules.auth.domain.enums.ProviderRegister;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortResponse {
  private Long id;
  private String username;
  private String fullName;
  private ProviderRegister provide;
  private String passwordInit;
  private String avatar;
  private String address;
  private String phone;
  private String email;
  private LocalDate birthDate;
  private boolean enabled;
  private List<String> roles;

  private Long employeeId;
  private Long warehouseId;
  private String positionName;
  private Long PositionId;
}