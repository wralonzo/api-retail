package com.wralonzo.detail_shop.domain.dto.client;

import lombok.Data;

@Data
public class ClientUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
}