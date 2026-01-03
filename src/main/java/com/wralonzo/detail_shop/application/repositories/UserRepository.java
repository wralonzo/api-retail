package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByUsername(String username);

        @Query("SELECT DISTINCT u FROM User u " +
                        "LEFT JOIN FETCH u.roles r " +
                        "LEFT JOIN FETCH u.employee e " +
                        "LEFT JOIN FETCH e.warehouse " +
                        "LEFT JOIN FETCH e.positionType " +
                        "WHERE (:roleName IS NULL OR r.name = :roleName) " +
                        "AND (:term IS NULL OR (LOWER(CAST(u.username AS text)) LIKE LOWER(CONCAT('%', :term, '%')) " +
                        "OR LOWER(CAST(u.fullName AS text)) LIKE LOWER(CONCAT('%', :term, '%'))))")
        Page<User> findAllWithFilters(@Param("term") String term,
                        @Param("roleName") String roleName,
                        Pageable pageable);

        @Query("SELECT u FROM User u " +
                        "LEFT JOIN FETCH u.employee e " +
                        "LEFT JOIN FETCH e.warehouse " + // <--- Importante para warehouseId/Name
                        "LEFT JOIN FETCH e.positionType " + // <--- Importante para positionId/Name
                        "LEFT JOIN FETCH u.roles " +
                        "WHERE u.id = :id")
        Optional<User> findByIdWithDetails(@Param("id") Long id);
}