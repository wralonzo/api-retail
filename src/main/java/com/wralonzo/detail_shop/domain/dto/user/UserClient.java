package com.wralonzo.detail_shop.domain.dto.user;

import com.wralonzo.detail_shop.domain.entities.Client;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserClient {
    private Client client;

    @NotEmpty(message = "El usuario debe tener al menos un rol asignado")
    private List<String> roles;
}
