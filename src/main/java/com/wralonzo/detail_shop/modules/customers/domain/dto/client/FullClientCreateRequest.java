package com.wralonzo.detail_shop.modules.customers.domain.dto.client;

import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserCreateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FullClientCreateRequest {
  @Valid
    @NotNull(message = "La información del usuario es obligatoria")
    private UserCreateRequest auth;

    @Valid
    @NotNull(message = "La información del cliente es obligatoria")
    private ClientRequest client;

    private boolean flagUser;
}
