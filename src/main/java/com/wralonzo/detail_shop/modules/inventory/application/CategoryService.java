package com.wralonzo.detail_shop.modules.inventory.application;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@AllArgsConstructor
@Builder
public class CategoryService {
  private CategoryRepository categoryRepository;

  public Page<Category> getAll(Pageable pageable) {
    return this.categoryRepository.findAll(pageable);
  }

  public Category create(Category category) {
    this.categoryRepository.findByName(category.getName())
        .orElseThrow(() -> new ResourceNotFoundException("Categoría ya existe " + category.getName()));
    return this.categoryRepository.save(category);
  }

  public Category update(Long id, Category category) {
    this.categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no existe " + id));
    return this.categoryRepository.save(category);
  }

  public void delete(Long id) {
    Category category = this.categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no existe " + id));
    category.setDeletedAt(LocalDateTime.now());
  }

  public Category getById(Long id) {
    Category category = this.categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no existe " + id));
    return category;
  }
}
