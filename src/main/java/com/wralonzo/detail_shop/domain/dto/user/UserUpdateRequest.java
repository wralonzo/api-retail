package com.wralonzo.detail_shop.domain.dto.user;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String phone;
    private String address;
    private String avatar;
    private List<String> roles;
    private Boolean enabled;
    private Long warehouseId;
    private Long positionTypeId;
}
