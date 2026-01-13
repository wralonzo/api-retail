package com.wralonzo.detail_shop.domain.dto.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

import com.wralonzo.detail_shop.domain.dto.user.UserShortResponse;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String notes;
    private LocalDate birthDate;
    private String clientType;
    private Long warehouseId;
    private String code;

    private UserShortResponse user;
}