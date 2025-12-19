package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.ProductRepository;
import com.wralonzo.detail_shop.domain.dto.product.ProductRequest;
import com.wralonzo.detail_shop.domain.dto.product.ProductUpdateRequest;
import com.wralonzo.detail_shop.domain.entities.Product;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.infrastructure.utils.UpdateUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product create(ProductRequest productRequest){
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        return this.productRepository.save(product);
    }

    public List<Product> getAll(){
        return this.productRepository.findAll();
    }

    public Product getById(Long id){
        return this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not exists."));
    }

    public Product update(ProductUpdateRequest productRequest){
        Product product = this.getById(productRequest.getId());
        UpdateUtil.updateIfPresent(productRequest.getName(), product::setName);
        UpdateUtil.updateIfPresent(productRequest.getDescription(), product::setDescription);
        UpdateUtil.updateIfPresent(productRequest.getPrice(), product::setPrice);
        UpdateUtil.updateIfPresent(productRequest.getStock(), product::setStock);
        return productRepository.save(product);
    }

    public void delete(Long id){
        this.getById(id);
        this.productRepository.deleteById(id);
    }

    public Product updateStock(ProductUpdateRequest productRequest){
        Product product = this.getById(productRequest.getId());
        Integer stock = productRequest.getStock() + product.getStock();
        UpdateUtil.updateIfPresent(stock, product::setStock);
        return productRepository.save(product);
    }
}
