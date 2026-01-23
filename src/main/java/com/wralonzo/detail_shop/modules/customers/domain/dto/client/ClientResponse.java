package com.wralonzo.detail_shop.modules.customers.domain.dto.client;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;
import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.profile.ProfileResponse;

@Data
@Builder
public class ClientResponse {
    // ID de la entidad Client
    private Long id;

    // --- Datos de Negocio (Módulo Customers) ---
    private String code;
    private ClientType clientType;
    private Long companyId;
    private String taxId;
    private LocalDate birthDate;

    // --- Identidad (Módulo Auth - Siempre presente) ---
    // Contiene: fullName, email, phone, address, avatar
    private ProfileResponse profile;

    // --- Credenciales de Acceso (Módulo Auth - Opcional) ---
    // Contiene: username, roles, enabled. Será null si el cliente no tiene usuario.
    private UserShortResponse user;
    private String preferredDeliveryAddress;
}