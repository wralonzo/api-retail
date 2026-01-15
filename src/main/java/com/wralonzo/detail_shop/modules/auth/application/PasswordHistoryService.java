package com.wralonzo.detail_shop.modules.auth.application;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PasswordHistory;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.PasswordHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@AllArgsConstructor
@Builder
public class PasswordHistoryService {
  private final PasswordHistoryRepository passwordHistoryRepository;
  private final PasswordEncoder passwordEncoder;

  public void validatePasswordHistory(User user, String newPassword) {

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      throw new ResourceConflictException("La contraseña actual debe ser direfente a la contraseña actual");
    }

    // --- VALIDACIÓN Historial de las últimas 5 ---
    List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());

    for (PasswordHistory oldPass : history) {
      if (passwordEncoder.matches(newPassword, oldPass.getPassword())) {
        throw new ResourceConflictException("La nueva contraseña no puede ser una de las últimas 5 utilizadas.");
      }
    }

    // --- VALIDACIÓN  Política de días (Chequeo de vencimiento opcional) ---
    // Esto se suele usar en el Login, pero aquí lo registramos

    // 4. Guardar en el Historial y Actualizar User
    PasswordHistory oldPassword = PasswordHistory.builder()
        .user(user)
        .password(user.getPassword()) // Guardamos la que acaba de dejar de usar
        .build();
    passwordHistoryRepository.save(oldPassword);
  }
}
