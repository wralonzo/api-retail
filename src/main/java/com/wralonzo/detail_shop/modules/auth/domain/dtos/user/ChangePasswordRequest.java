package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ChangePasswordRequest {
  @NotBlank(message = "La nueva contraseña no puede estar vacía")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  private String newPassword;

  private String motive;

  @Size(max = 60, message = "El canal no debe estar vacío")
  private String channel;

  private String ipLocal;

}