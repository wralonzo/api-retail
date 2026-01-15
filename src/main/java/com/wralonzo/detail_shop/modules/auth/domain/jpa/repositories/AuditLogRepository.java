package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  // Cuenta intentos de una acción específica para un usuario en un rango de
  // tiempo
  @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username " +
      "AND a.action = :action " +
      "AND a.timestamp >= :since")
  long countUserActionsAfter(
      @Param("username") String username,
      @Param("action") String action,
      @Param("since") LocalDateTime since);

  // Cuenta cuántos usuarios distintos han sido atacados desde una misma IP
  @Query("SELECT COUNT(DISTINCT a.username) FROM AuditLog a " +
      "WHERE a.ipAddress = :ip " +
      "AND a.action = :action " +
      "AND a.timestamp >= :since")
  long countDistinctUsersByIpAndAction(
      @Param("ip") String ip,
      @Param("action") String action,
      @Param("since") LocalDateTime since);

  @Modifying
  @Query("DELETE FROM AuditLog a WHERE a.timestamp < :limit")
  int deleteByTimestampBefore(@Param("limit") LocalDateTime limit);
}
