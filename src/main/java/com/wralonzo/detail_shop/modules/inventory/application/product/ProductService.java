package com.wralonzo.detail_shop.modules.inventory.application.product;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductUnitDto;
import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryMovementService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBundleDto;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitRepository;
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
    private final ProductUnitRepository productUnitRepository;
    private final InventoryMovementService inventoryMovementService;
    private final CategoryRepository categoryRepository;

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

        if (request.getType() == ProductType.BUNDLE && request.getBundleItems() == null) {
            throw new ResourceConflictException("Ya existe un producto con el SKU: " + request.getSku());
        }

        if (categoryRepository.findById(request.getCategoryId()).isEmpty()) {
            throw new ResourceConflictException("No existe una categoría con el ID: " + request.getCategoryId());
        }

        UserBusinessContext context = warehouseService.getUserBusinessContext();

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pricePurchase(request.getPricePurchase())
                .basePrice(request.getPriceSale())
                .barcode(request.getBarcode())
                .sku(request.getSku().toUpperCase())
                .active(true)
                .type(request.getType() != null ? request.getType() : ProductType.STANDARD)
                .companyId(context.companyId())
                .build();

        // 1. Manejo de Unidades Adicionales
        product.setUnits(handleProductUnits(product, request.getUnits()));

        // 2. Manejo de Combos (Bundles)
        handleProductBundles(product, request.getBundleItems());

        List<Long> branches = warehouseService.findAllIdsByCompanyId(context.companyId());

        for (Long branchId : branches) {
            // 1. Asegurar ProductBranchConfig
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

            ProductBranchConfig config = ProductBranchConfig.builder()
                    .product(product)
                    .branchId(branchId)
                    .active(true)
                    .stockMinim(request.getStockMinim())
                    .category(category)
                    .build();

            // Agregar a la colección del producto
            product.getBranchConfigs().add(config);

            // 2. Definir Precio (si hay unidades disponibles)
            if (product.getUnits() != null && !product.getUnits().isEmpty()) {
                ProductBranchPrice newPrice = ProductBranchPrice.builder()
                        .branchConfig(config)
                        .unit(product.getUnits().get(0).getUnitProduct())
                        .price(product.getBasePrice())
                        .active(true)
                        .build();

                config.getPrices().add(newPrice);
            }
        }

        // 3. Inicializar inventario en cada almacén de la sucursal
        for (Long warehouseId : context.warehouseIds()) {
            Inventory inventory = inventoryMovementService.createInitialInventory(null, warehouseId);
            inventory.setProduct(product); // Relación bidireccional
            product.getInventories().add(inventory);
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
            product.setUnits(handleProductUnits(product, request.getUnits()));
        }

        // Actualizar Bundle Items
        if (product.getType() == ProductType.BUNDLE && request.getBundleItems() != null) {
            product.getBundleItems().clear();
            handleProductBundles(product, request.getBundleItems());
        }

        return mapToResponse(productRepository.save(product));
    }

    private List<ProductUnitDetails> handleProductUnits(Product product, List<ProductUnitDto> unitsDto) {
        if (unitsDto != null) {
            List<ProductUnitDetails> units = unitsDto.stream().map(dto -> {
                ProductUnit unit = this.productUnitRepository.findById(dto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Unidad no encontrada ID: " + dto.getId()));
                return ProductUnitDetails.builder()
                        .product(product)
                        .unitProduct(unit)
                        .build();
            }).toList();
            product.getUnits().addAll(units);
            return units;
        }
        return null;
    }

    private void handleProductBundles(Product product, List<ProductBundleDto> bundlesDto) {
        if (product.getType() == ProductType.BUNDLE && bundlesDto != null) {
            List<ProductBundle> bundles = bundlesDto.stream().map(dto -> {
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
                .bundleItems(p.getType() == ProductType.BUNDLE ? p.getBundleItems().stream().map(b -> {
                    ProductBundleDto dto = new ProductBundleDto();
                    dto.setChildProductId(b.getComponentProduct().getId());
                    dto.setQuantity(b.getQuantity());
                    return dto;
                }).toList() : null)
                .build();
    }
}