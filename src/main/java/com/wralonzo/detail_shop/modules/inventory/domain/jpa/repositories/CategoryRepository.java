package com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByName(String name);

  Optional<Category> findByCode(String code);
}
