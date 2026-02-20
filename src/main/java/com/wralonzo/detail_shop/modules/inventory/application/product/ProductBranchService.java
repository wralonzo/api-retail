package com.wralonzo.detail_shop.modules.inventory.application.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBranchConfigDto;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchConfig;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.organization.application.BranchService;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductBranchService {

    private final ProductBranchConfigRepository productBranchConfigRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseService warehouseService;
    private final BranchService branchService;

    @Transactional
    public ProductBranchConfigDto create(Long idProduct, ProductBranchConfigDto dto) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        ProductBranchConfig productBranchConfig = ProductBranchConfig.builder()
                .product(product)
                .category(category)
                .active(dto.getActive())
                .stockMinim(dto.getStockMinim())
                .build();
        return mapToResponse(productBranchConfigRepository.save(productBranchConfig));
    }

    @Transactional
    public ProductBranchConfigDto update(Long idProduct, Long idProductBranchConfig, ProductBranchConfigDto dto) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        ProductBranchConfig productBranchConfig = productBranchConfigRepository.findById(idProductBranchConfig)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        productBranchConfig.setProduct(product);
        productBranchConfig.setCategory(category);
        productBranchConfig.setActive(dto.getActive());
        productBranchConfig.setStockMinim(dto.getStockMinim());
        return mapToResponse(productBranchConfigRepository.save(productBranchConfig));
    }

    @Transactional
    public void delete(Long idProductBranchConfig) {
        productBranchConfigRepository.findById(idProductBranchConfig)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        productBranchConfigRepository.deleteById(idProductBranchConfig);
    }

    @Transactional(readOnly = true)
    public List<ProductBranchConfigDto> getAll(Long idProduct) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        List<ProductBranchConfigDto> productBranchConfigs = product.getBranchConfigs().stream().map(this::mapToResponse)
                .toList();
        productBranchConfigs.forEach(productBranchConfig -> {
            productBranchConfig
                    .setBranchName(branchService.findById(productBranchConfig.getBranchId()).getName());
        });
        return productBranchConfigs;
    }

    @Transactional(readOnly = true)
    public ProductBranchConfigDto getOneRecord(Long idProductBranchConfig) {
        ProductBranchConfig productBranchConfig = productBranchConfigRepository.findById(idProductBranchConfig)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        return mapToResponse(productBranchConfig);
    }

    private ProductBranchConfigDto mapToResponse(ProductBranchConfig productBranchConfig) {
        return ProductBranchConfigDto.builder()
                .id(productBranchConfig.getId())
                .branchId(productBranchConfig.getBranchId())
                .categoryId(productBranchConfig.getCategory().getId())
                .categoryName(productBranchConfig.getCategory().getName())
                .active(productBranchConfig.getActive())
                .stockMinim(productBranchConfig.getStockMinim())
                .build();
    }
}
