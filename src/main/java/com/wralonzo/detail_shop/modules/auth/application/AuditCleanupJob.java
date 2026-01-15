package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditCleanupJob {

  private final AuditLogRepository auditLogRepository;

  /**
   * Se ejecuta todos los domingos a las 3:00 AM
   * Cron: Segundo, Minuto, Hora, Día del mes, Mes, Día de la semana
   */
  @Scheduled(cron = "0 0 3 * * SUN")
  @Transactional
  public void cleanupOldLogs() {
    LocalDateTime retentionLimit = LocalDateTime.now().minusMonths(6);

    log.info("Iniciando limpieza de logs de auditoría anteriores a: {}", retentionLimit);

    // Debes agregar este método en tu Repository
    int deletedRows = auditLogRepository.deleteByTimestampBefore(retentionLimit);

    log.info("Limpieza completada. Se eliminaron {} registros de auditoría.", deletedRows);
  }
}