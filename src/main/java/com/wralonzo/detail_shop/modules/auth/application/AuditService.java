package com.wralonzo.detail_shop.modules.auth.application;

import java.time.LocalDateTime;

import org.apache.coyote.BadRequestException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.AuditLog;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.AuditLogRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuditService {
  private final AuditLogRepository auditLogRepository;

  @Async // Para que no bloquee la ejecución principal
  public void logAction(String username, String action, String description, String channel, String ipAddress) {
    AuditLog log = AuditLog.builder()
        .username(username)
        .action(action)
        .channel(channel)
        .ipAddress(channel)
        .description(description)
        .timestamp(LocalDateTime.now())
        .build();
    auditLogRepository.save(log);
  }

  public void validatePasswordChangeAttempts(String username, String action) throws BadRequestException {
    LocalDateTime threshold = LocalDateTime.now().minusHours(24);

    long attempts = auditLogRepository.countUserActionsAfter(
        username,
        action,
        threshold);

    // Límite de 3 cambios o intentos de cambio en un día
    if (attempts >= 5) {
      throw new BadRequestException("Has excedido el límite de cambios de contraseña permitidos por hoy (máximo 3).");
    }
  }

  public void validateIpAbuseThreshold(String ipAddress, String action) throws BadRequestException {
    // Ventana de tiempo más corta para ataques de red (ej. 1 hora)
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    long distinctUsers = auditLogRepository.countDistinctUsersByIpAndAction(
        ipAddress,
        action,
        oneHourAgo);

    if (distinctUsers >= 5) {
      // Logueamos internamente este evento sospechoso
      System.err.println("ALERTA DE SEGURIDAD: IP sospechosa detectada: " + ipAddress);
      throw new BadRequestException("Tu dirección de red ha sido bloqueada temporalmente por actividad sospechosa.");
    }
  }
}