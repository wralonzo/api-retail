package com.wralonzo.detail_shop.modules.customers.domain.dto.client;
import lombok.Builder;
import lombok.Data;

import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;

@Data
@Builder
public class ClientRequest {

    private ClientType clientType;

    private String taxId;

    private Boolean flagUser;

    private String preferredDeliveryAddress;

    private Long companyId;
}
