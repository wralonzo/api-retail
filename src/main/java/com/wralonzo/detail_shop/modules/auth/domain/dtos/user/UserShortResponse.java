
package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortResponse {
  private Long id;
  private String username;
  private String fullName;
  private String provide;
  private String passwordInit;
  private boolean enabled;
  private List<String> roles;
}