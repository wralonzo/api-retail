package com.wralonzo.detail_shop.modules.organization.domain.dtos.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {
    @NotBlank(message = "El nombre de la empresa no debe estar vacío")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String businessName;

    @NotBlank(message = "El NIT/RUC/RFC es requerido")
    @Size(max = 10, message = "El NIT/RUC/RFC no debe exceder 10 caracteres")
    private String taxId;

    @Size(max = 100, message = "La dirección no debe exceder 100 caracteres")
    private String address;

    @Size(max = 50, message = "El teléfono no debe exceder 50 caracteres")
    private String phone;

    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 50, message = "El correo no debe exceder 50 caracteres")
    private String email;
}
