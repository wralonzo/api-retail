package com.wralonzo.detail_shop.domain.dto.client;

import com.wralonzo.detail_shop.domain.enums.ClientType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ClientRequest {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "El teléfono debe contener entre 8 y 15 dígitos")
    private String phone;

    private String address;

    @Size(max = 100, message = "Las notas no pueden exceder los 100 caracteres")
    private String notes;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate birthDate;

    @NotNull(message = "El tipo de cliente es obligatorio")
    private ClientType clientType;

    @Positive(message = "ID de almacén inválido")
    private Long warehouseId;

    private Boolean flagUser;
}
