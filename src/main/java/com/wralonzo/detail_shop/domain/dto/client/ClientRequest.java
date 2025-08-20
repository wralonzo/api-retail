package com.wralonzo.detail_shop.domain.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientRequest {
    @NotBlank(message = "El campo 'name' es obligatorio y no debe estar vacío.")
    private String name;

    @NotBlank(message = "El campo 'address' es obligatorio y no debe estar vacío.")
    private String address;

    @Email(message = "El formato del email no es válido.")
    @NotBlank(message = "El campo 'email' es obligatorio y no debe estar vacío.")
    private String email;

    @NotBlank(message = "El campo 'phone' es obligatorio y no debe estar vacío.")
    private String phone;
}
