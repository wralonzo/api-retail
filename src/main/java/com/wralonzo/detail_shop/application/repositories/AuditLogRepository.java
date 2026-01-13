package com.wralonzo.detail_shop.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.domain.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
