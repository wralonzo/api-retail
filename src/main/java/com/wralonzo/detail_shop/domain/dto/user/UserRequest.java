package com.wralonzo.detail_shop.domain.dto.user;

import com.wralonzo.detail_shop.domain.entities.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class UserRequest {
    private User user;

    @NotBlank(message = "El campo 'positionType' es obligatorio y no debe estar vacío.")
    private Long positionType;

    @Size(message = "La descripción no puede superar los 255 caracteres.")
    private Long warehouse;

    //example "roles": ["ROLE_VENDEDOR", "ROLE_FACTURADOR"]
    @NotEmpty(message = "El usuario debe tener al menos un rol asignado")
    private List<String> roles;
}
