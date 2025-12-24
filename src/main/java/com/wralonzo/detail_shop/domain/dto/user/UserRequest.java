package com.wralonzo.detail_shop.domain.dto.user;

import com.wralonzo.detail_shop.domain.entities.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {
    private User user;

    @NotBlank(message = "El campo 'positionType' es obligatorio y no debe estar vacío.")
    private Long positionType;

    @Size(message = "La descripción no puede superar los 255 caracteres.")
    private Long warehouse;
}
