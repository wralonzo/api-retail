package com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PasswordHistory;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
  // Spring Data genera automáticamente la consulta:
  // SELECT * FROM auth.password_history WHERE user_id = ? ORDER BY created_at
  // DESC LIMIT 5
  List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
