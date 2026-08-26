package com.wralonzo.detail_shop.modules.promotions.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.promotions.domain.jpa.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND (p.startDate IS NULL OR p.startDate <= :today) AND (p.endDate IS NULL OR p.endDate >= :today) ORDER BY p.id DESC")
    List<Promotion> findActivePromotions(LocalDate today);
}
