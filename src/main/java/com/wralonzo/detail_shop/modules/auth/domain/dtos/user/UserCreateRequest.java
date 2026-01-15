package com.wralonzo.detail_shop.modules.auth.domain.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class UserCreateRequest {

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

  private Set<String> roles; // Nombres de los roles (ej: "ROLE_CLIENTE")
}