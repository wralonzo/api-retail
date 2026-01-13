package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.CategoryRepository;
import com.wralonzo.detail_shop.application.repositories.ProductRepository;
import com.wralonzo.detail_shop.application.repositories.SupplierRepository;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.application.specifications.ProductSpecifications;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.product.ProductRequest;
import com.wralonzo.detail_shop.domain.dto.product.ProductResponse;
import com.wralonzo.detail_shop.domain.entities.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(String term, Boolean active, Long categoryId, Pageable pageable) {
        Specification<Product> spec = Specification
                .where(ProductSpecifications.searchByTerm(term)) // Usamos el nuevo buscador global
                .and(ProductSpecifications.isActive(active))
                .and(ProductSpecifications.hasCategory(categoryId));

        return productRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    // --- 1. OBTENER POR ID
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToResponse(product);
    }

    // --- 2. BÚSQUEDA FILTRADA Y PAGINADA
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String term, Pageable pageable) {
        // Asumiendo que crearás este método en el repository
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(term, term, pageable)
                .map(this::mapToResponse);
    }

    // --- 3. CREAR PRODUCTO
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceConflictException("Ya existe un producto con el SKU: " + request.getSku());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pricePurchase(request.getPricePurchase())
                .priceSale(request.getPriceSale())
                .barcode(request.getBarcode())
                .stockMinim(request.getStockMinim())
                .sku(request.getSku().toUpperCase())
                .active(true)
                .category(categoryRepository.getReferenceById(request.getCategoryId()))
                .build();

        return mapToResponse(productRepository.save(product));
    }

    // --- 4. ACTUALIZAR (PATCH)
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Validar SKU si está cambiando
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new ResourceConflictException("El nuevo SKU ya está en uso");
            }
            product.setSku(request.getSku());
        }

        // Actualización parcial
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getPriceSale() != null)
            product.setPriceSale(request.getPriceSale());
        if (request.getPricePurchase() != null)
            product.setPricePurchase(request.getPricePurchase());
        if (request.getCategoryId() != null)
            product.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        if (request.getBarcode() != null)
            product.setBarcode(request.getBarcode());

        // El updateAt se llena solo por la anotación @LastModifiedDate
        return mapToResponse(productRepository.save(product));
    }

    // --- 5. ELIMINACIÓN LÓGICA (SOFT DELETE)
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        product.setDeletedAt(LocalDateTime.now());
        product.setActive(false);
        productRepository.save(product);
    }

    // --- MAPPER HELPER
    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .pricePurchase(p.getPricePurchase())
                .priceSale(p.getPriceSale())
                .stockMinim(p.getStockMinim())
                .active(p.getActive())
                // Mapeo de Categoría
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "Sin Categoría")
                // Mapeo de Proveedor
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : "Sin Proveedor")
                // Mapeo de Almacén
                .warehouseId(p.getWarehouse() != null ? p.getWarehouse().getId() : null)
                .warehouseName(p.getWarehouse() != null ? p.getWarehouse().getName() : "Sin Almacén")
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdateAt())
                .build();
    }
}