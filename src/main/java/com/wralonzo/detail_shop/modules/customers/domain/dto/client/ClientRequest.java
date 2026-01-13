package com.wralonzo.detail_shop.modules.customers.domain.dto.client;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;

@Data
@Builder
public class ClientRequest {

    private ClientType clientType;

    @NotNull(message = "El ID de la compañia es requerido")
    private Long companyId;

    private String taxId;

    private Boolean flagUser;

    private String preferredDeliveryAddress;
}
