package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.enums.ClientType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
  boolean existsByEmail(String email);

  @Query("SELECT c FROM Client c WHERE " +
      "(:clientType IS NULL OR c.clientType = :clientType) AND " +
      "(CAST(:term AS string) IS NULL OR " + // <-- El CAST soluciona el error de bytea
      "LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%')) OR " +
      "LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:term AS string), '%'))) " +
      "AND c.deletedAt IS NULL")
  Page<Client> searchClients(@Param("term") String term,
      @Param("clientType") ClientType clientType,
      Pageable pageable);

  Optional<Client> findFirstByOrderByCodeDesc();

  @Query("SELECT c FROM Client c " +
      "LEFT JOIN FETCH c.warehouse " +
      "LEFT JOIN FETCH c.user " +
      "WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<Client> findByIdWithDetails(@Param("id") Long id);
}