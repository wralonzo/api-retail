
package com.wralonzo.detail_shop.domain.dto.user;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortResponse {
  private Integer id;
  private String username;
  private String fullName;
  private String passwordInit;
  private List<String> roles;
}