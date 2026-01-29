package com.wralonzo.detail_shop.modules.auth.domain.dtos.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionsRequest {
    @NotEmpty(message = "La lista de permisos no debe estar vacía")
    private List<Long> permissionIds;
}
