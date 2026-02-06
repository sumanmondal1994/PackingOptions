package com.project.packingoptions.service;

import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.dto.ProductUpdateRequest;
import com.project.packingoptions.exception.ResourceAlreadyExistsException;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.PackagingOptionRepository;
import com.project.packingoptions.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final PackagingOptionRepository packagingOptionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Retrieving all products");
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductByCode(String code) {
        log.info("Retrieving product with code: {}", code);
        return productRepository.findByCode(code);
    }

    @Override
    public Product createProduct(ProductRequest request) {
        log.info("Creating product with code: {}", request.getCode());

        if (productRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Product", "code", request.getCode());
        }

        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .basePrice(request.getBasePrice())
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(String code, ProductUpdateRequest request) {
        log.info("Updating product with code: {}", code);

        Product existingProduct = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", code));

        // Update the existing product (code remains unchanged)
        existingProduct.setName(request.getName());
        existingProduct.setBasePrice(request.getBasePrice());

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(String code) {
        log.info("Deleting product with code: {}", code);

        if (!productRepository.existsByCode(code)) {
            throw new ResourceNotFoundException("Product", "code", code);
        }

        // Delete associated packaging options first
        packagingOptionRepository.deleteByProductCode(code);

        productRepository.deleteByCode(code);
    }
}
