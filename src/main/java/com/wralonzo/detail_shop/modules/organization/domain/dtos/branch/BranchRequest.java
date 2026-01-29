package com.wralonzo.detail_shop.modules.organization.domain.dtos.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    @NotBlank(message = "El nombre de la sucursal no debe estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    private String code;

    @Size(max = 100, message = "La dirección no debe exceder 100 caracteres")
    private String address;

    @NotNull(message = "El ID de la compañía es requerido")
    private Long companyId;
}
