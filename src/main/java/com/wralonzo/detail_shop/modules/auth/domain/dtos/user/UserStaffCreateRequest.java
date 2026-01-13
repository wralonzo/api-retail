package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserStaffCreateRequest {

  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 4, max = 50)
  private String username;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 8)
  private String password;

  // --- Datos para el Perfil ---
  @NotBlank(message = "El nombre completo es obligatorio")
  private String fullName;

  @Email(message = "El correo no es válido")
  private String email;

  @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
  private LocalDate birthDate;

  private String phone;
  private String address;
  private String avatar;

  private long positionTypeId;
  private long warehouseId;

  @NotEmpty(message = "El usuario debe tener al menos un rol asignado")
  private List<String> roles;
}