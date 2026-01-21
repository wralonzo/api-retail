package com.wralonzo.detail_shop.modules.inventory.application.product;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductUnitDto;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBundleDto;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.specs.ProductSpecifications;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import java.util.List;

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
    private final WarehouseService warehouseService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(String term, Boolean active, Long requestedCompanyId,
            Pageable pageable) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();

        // --- LÓGICA DE SEGURIDAD CORPORATIVA ---
        Long finalCompanyId;

        if (context.isSuperAdmin()) {
            // El Admin puede ver lo que pida del front, o todo si viene null
            finalCompanyId = requestedCompanyId;
        } else {
            // Un usuario normal SIEMPRE está limitado a su propia compañía
            finalCompanyId = context.companyId();
        }

        Specification<Product> spec = Specification
                .where(ProductSpecifications.searchByTerm(term))
                .and(ProductSpecifications.isActive(active))
                .and(ProductSpecifications.hasCompany(finalCompanyId)) // Filtro por empresa
                .and((root, query, cb) -> cb.isNull(root.get("deletedAt"))); // No mostrar eliminados

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
                .basePrice(request.getPriceSale())
                .barcode(request.getBarcode())
                .sku(request.getSku().toUpperCase())
                .active(true)
                .type(request.getType() != null ? request.getType() : ProductType.STANDARD)
                .build();

        // 1. Manejo de Unidades Adicionales
        if (request.getUnits() != null) {
            List<ProductUnit> units = request.getUnits().stream().map(dto -> ProductUnit.builder()
                    .product(product)
                    .unitName(dto.getUnitName())
                    .conversionFactor(dto.getConversionFactor())
                    .barcode(dto.getBarcode())
                    .isBase(false)
                    .build()).toList();
            product.getUnits().addAll(units);
        }

        // 2. Manejo de Combos (Bundles)
        if (product.getType() == ProductType.BUNDLE && request.getBundleItems() != null) {
            List<ProductBundle> bundles = request.getBundleItems().stream().map(dto -> {
                Product child = productRepository.findById(dto.getChildProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto hijo no encontrado ID: " + dto.getChildProductId()));
                return ProductBundle.builder()
                        .comboProduct(product)
                        .componentProduct(child)
                        .quantity(dto.getQuantity())
                        .build();
            }).toList();
            product.getBundleItems().addAll(bundles);
        }

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

        // Actualización parcial básica
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getPriceSale() != null)
            product.setBasePrice(request.getPriceSale());
        if (request.getPricePurchase() != null)
            product.setPricePurchase(request.getPricePurchase());
        if (request.getBarcode() != null)
            product.setBarcode(request.getBarcode());
        if (request.getType() != null)
            product.setType(request.getType());

        // Actualizar Unidades (Reemplazo simple por ahora, idealmente merge)
        if (request.getUnits() != null) {
            product.getUnits().clear();
            List<ProductUnit> units = request.getUnits().stream().map(dto -> ProductUnit.builder()
                    .product(product)
                    .unitName(dto.getUnitName())
                    .conversionFactor(dto.getConversionFactor())
                    .barcode(dto.getBarcode())
                    .isBase(false)
                    .build()).toList();
            product.getUnits().addAll(units);
        }

        // Actualizar Bundle Items
        if (product.getType() == ProductType.BUNDLE && request.getBundleItems() != null) {
            product.getBundleItems().clear();
            List<ProductBundle> bundles = request.getBundleItems().stream().map(dto -> {
                Product child = productRepository.findById(dto.getChildProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto hijo no encontrado ID: " + dto.getChildProductId()));
                return ProductBundle.builder()
                        .comboProduct(product)
                        .componentProduct(child)
                        .quantity(dto.getQuantity())
                        .build();
            }).toList();
            product.getBundleItems().addAll(bundles);
        }

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

    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .pricePurchase(p.getPricePurchase())
                .priceSale(p.getBasePrice()) // Base Price as default sale price reference
                .active(p.getActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdateAt())
                .type(p.getType())
                .units(p.getUnits().stream().map(u -> {
                    ProductUnitDto dto = new ProductUnitDto();
                    dto.setUnitName(u.getUnitName());
                    dto.setConversionFactor(u.getConversionFactor());
                    dto.setBarcode(u.getBarcode());
                    return dto;
                }).toList())
                .bundleItems(p.getType() == ProductType.BUNDLE ? p.getBundleItems().stream().map(b -> {
                    ProductBundleDto dto = new ProductBundleDto();
                    dto.setChildProductId(b.getComponentProduct().getId());
                    dto.setQuantity(b.getQuantity());
                    return dto;
                }).toList() : null)
                .build();
    }
}