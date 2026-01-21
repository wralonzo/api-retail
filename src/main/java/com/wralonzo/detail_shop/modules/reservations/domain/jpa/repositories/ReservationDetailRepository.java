package com.wralonzo.detail_shop.modules.reservations.domain.jpa.repositories;

import com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities.ReservationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
}
