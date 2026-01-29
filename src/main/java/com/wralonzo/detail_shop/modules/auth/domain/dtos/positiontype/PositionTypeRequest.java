package com.wralonzo.detail_shop.modules.auth.domain.dtos.positiontype;

import jakarta.validation.constraints.Min;
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
public class PositionTypeRequest {
    @NotBlank(message = "El nombre del tipo de posición no debe estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotNull(message = "El nivel es requerido")
    @Min(value = 1, message = "El nivel debe ser al menos 1")
    private Integer level;
}
