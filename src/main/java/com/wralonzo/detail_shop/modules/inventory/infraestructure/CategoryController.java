package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.inventory.application.CategoryService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.category.CategoryRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("category")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping()
  public ResponseEntity<Page<Category>> getAll(
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    Page<Category> category = categoryService.getAll(pageable);
    return ResponseUtil.ok(category);

  }

  @PostMapping()
  public ResponseEntity<Category> create(
      @Valid @RequestBody CategoryRequest request) {
    Category category = this.categoryService.createCategory(request);
    return ResponseUtil.ok(category);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Category> findOne(@PathVariable Long id) {
    Category category = this.categoryService.getById(id);
    return ResponseUtil.ok(category);
  }

  @PatchMapping("/{id}/delete")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> deactivate(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Category request) {
    categoryService.update(id, request);
    return ResponseUtil.ok(Map.of("message", "Recurso eliminado exitosamente"));
  }
}
