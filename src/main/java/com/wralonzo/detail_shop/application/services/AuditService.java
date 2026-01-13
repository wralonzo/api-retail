package com.wralonzo.detail_shop.application.services;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.application.repositories.AuditLogRepository;
import com.wralonzo.detail_shop.domain.entities.AuditLog;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuditService {
  private final AuditLogRepository auditLogRepository;

  @Async // Para que no bloquee la ejecución principal
  public void logAction(String username, String action, String description) {
    AuditLog log = AuditLog.builder()
        .username(username)
        .action(action)
        .description(description)
        .timestamp(LocalDateTime.now())
        .build();
    auditLogRepository.save(log);
  }
}