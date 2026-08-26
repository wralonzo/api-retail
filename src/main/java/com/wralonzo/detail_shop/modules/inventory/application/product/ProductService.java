package com.wralonzo.detail_shop.modules.inventory.application.product;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.specs.ProductSpecifications;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(String term, Boolean active, Boolean isService, Long requestedCompanyId,
            Pageable pageable) {
        UserBusinessContext context = null;
        Long finalCompanyId = requestedCompanyId;
        try {
            context = warehouseService.getUserBusinessContext();
            if (context != null && !context.isSuperAdmin()) {
                finalCompanyId = context.companyId();
            }
        } catch (Exception e) {
            // Cliente público
        }

        Specification<Product> spec = Specification
                .where(ProductSpecifications.searchByTerm(term))
                .and(ProductSpecifications.isActive(active))
                .and(ProductSpecifications.isService(isService))
                .and(ProductSpecifications.hasCompany(finalCompanyId))
                .and((root, query, cb) -> cb.isNull(root.get("deletedAt")));

        return productRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String term, Pageable pageable) {
        return getAll(term, true, null, null, pageable);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Long companyId = 1L;
        try {
            UserBusinessContext context = warehouseService.getUserBusinessContext();
            if (context != null) companyId = context.companyId();
        } catch (Exception e) {
            // Client público
        }

        BigDecimal price = request.getPriceSale() != null ? request.getPriceSale() : BigDecimal.valueOf(35.00);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku() != null ? request.getSku() : "PROD-" + System.currentTimeMillis())
                .barcode(request.getBarcode())
                .companyId(companyId)
                .pricePurchase(request.getPricePurchase())
                .basePrice(price)
                .type(request.getType() != null ? request.getType() : ProductType.STANDARD)
                .isService(request.getType() == ProductType.SERVICE)
                .active(request.isActive())
                .build();

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPriceSale() != null) product.setBasePrice(request.getPriceSale());
        if (request.getPricePurchase() != null) product.setPricePurchase(request.getPricePurchase());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());

        productRepository.save(product);
        return mapToResponse(product);
    }

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
                .basePrice(p.getBasePrice())
                .priceSale(p.getBasePrice())
                .active(p.getActive())
                .isService(p.getIsService() != null ? p.getIsService() : p.getType() == ProductType.SERVICE)
                .type(p.getType())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdateAt())
                .build();
    }
}