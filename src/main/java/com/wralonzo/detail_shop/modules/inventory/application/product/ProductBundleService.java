package com.wralonzo.detail_shop.modules.inventory.application.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ProductBundleDto;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBundle;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBundleRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductBundleService {

    private final ProductBundleRepository productBundleRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductBundleDto create(Long idProduct, ProductBundleDto dto) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Product productChild = productRepository.findById(dto.getChildProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        ProductBundle productBundle = ProductBundle.builder()
                .comboProduct(product)
                .componentProduct(productChild)
                .quantity(dto.getQuantity())
                .build();
        return mapToResponse(productBundleRepository.save(productBundle));
    }

    @Transactional
    public ProductBundleDto update(Long idProduct, Long idProductBundle, ProductBundleDto dto) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Product productChild = productRepository.findById(dto.getChildProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        ProductBundle productBundle = productBundleRepository.findById(idProductBundle)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        productBundle.setComboProduct(product);
        productBundle.setComponentProduct(productChild);
        productBundle.setQuantity(dto.getQuantity());
        return mapToResponse(productBundleRepository.save(productBundle));
    }

    @Transactional
    public void delete(Long idProductBundle) {
        productBundleRepository.findById(idProductBundle)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        productBundleRepository.deleteById(idProductBundle);
    }

    @Transactional(readOnly = true)
    public ProductBundleDto getOneRecord(Long idProductBundle) {
        ProductBundle productBundle = productBundleRepository.findById(idProductBundle)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de producto no encontrada"));
        return mapToResponse(productBundle);
    }

    @Transactional(readOnly = true)
    public List<ProductBundleDto> getAll(Long idProduct) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return product.getBundleItems().stream().map(this::mapToResponse).toList();
    }

    private ProductBundleDto mapToResponse(ProductBundle productBundle) {
        return ProductBundleDto.builder()
                .childProductId(productBundle.getComponentProduct().getId())
                .quantity(productBundle.getQuantity())
                .childProductName(productBundle.getComponentProduct().getName())
                .build();
    }
}
