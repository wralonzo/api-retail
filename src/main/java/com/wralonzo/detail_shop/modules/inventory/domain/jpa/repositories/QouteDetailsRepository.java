package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.QuoteDetails;

@Repository
public interface QouteDetailsRepository extends JpaRepository<QuoteDetails, Long> {

}
