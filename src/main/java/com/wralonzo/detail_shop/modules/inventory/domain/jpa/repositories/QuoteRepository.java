package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Quote;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long>, JpaSpecificationExecutor<Quote> {
    Optional<Quote> findByReferenceNumber(String referenceNumber);
}
