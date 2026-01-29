package com.wralonzo.detail_shop.modules.organization.domain.dtos.warehouse;

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
public class WarehouseRequest {
    @NotBlank(message = "El nombre del almacén no debe estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotBlank(message = "El teléfono es requerido")
    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    private String phone;

    @NotBlank(message = "El código es requerido")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    private String code;

    @NotNull(message = "El ID de la sucursal es requerido")
    private Long branchId;

    @Builder.Default
    private Boolean active = true;
}
