package com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
  boolean existsByEmail(String email);



  Optional<Client> findFirstByOrderByCodeDesc();

  @Query("SELECT c FROM Client c " +
      "LEFT JOIN FETCH c.warehouse " +
      "LEFT JOIN FETCH c.user " +
      "WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<Client> findByIdWithDetails(@Param("id") Long id);
}