package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
