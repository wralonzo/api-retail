package com.wralonzo.detail_shop.modules.inventory.application;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.category.CategoryRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@AllArgsConstructor
@Builder
public class CategoryService {
  private CategoryRepository categoryRepository;
  private final WarehouseService warehouseService;

  public Page<Category> getAll(Pageable pageable) {
    UserBusinessContext context = warehouseService.getUserBusinessContext();
    return categoryRepository.findByBranchIdAndDeletedAtIsNull(context.branchId(), pageable);
  }

  @Transactional
  public Category createCategory(CategoryRequest request) {
    UserBusinessContext context = warehouseService.getUserBusinessContext();

    // Si el request no trae warehouseId, usamos el del empleado por defecto
    Long targetBranchId = (request.getBranchId() != null)
        ? request.getBranchId()
        : context.branchId();

    Category category = Category.builder()
        .name(request.getName())
        .code(request.getCode())
        .branchId(targetBranchId)
        .notes(request.getNotes())
        .build();

    return categoryRepository.save(category);
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
