package com.project.packingoptions.service;

import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.dto.ProductUpdateRequest;
import com.project.packingoptions.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();

    Optional<Product> getProductByCode(String code);

    Product createProduct(ProductRequest request);

    Product updateProduct(String code, ProductUpdateRequest request);

    void deleteProduct(String code);
}
