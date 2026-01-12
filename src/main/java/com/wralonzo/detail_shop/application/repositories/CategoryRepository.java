package com.wralonzo.detail_shop.application.repositories;

import com.wralonzo.detail_shop.domain.entities.Category;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByName(String name);
}
