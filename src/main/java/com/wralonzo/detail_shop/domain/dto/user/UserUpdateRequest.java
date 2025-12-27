package com.wralonzo.detail_shop.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Formato de teléfono inválido")
    private String phone;

    private String address;
    private String avatar;

    @NotEmpty(message = "El usuario debe tener al menos un rol asignado")
    private List<String> roles;

    @NotNull(message = "El estado (enabled) es obligatorio")
    private Boolean enabled;

    @NotNull(message = "El almacén asignado es obligatorio")
    private Long warehouseId;

    @NotNull(message = "El tipo de puesto es obligatorio")
    private Long positionTypeId;

}
