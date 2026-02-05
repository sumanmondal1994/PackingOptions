package com.project.packingoptions.service;

import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.exception.ResourceAlreadyExistsException;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.ProductRepository;
import com.project.packingoptions.util.TestDataFactory;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;
    private Faker faker;
    private Product product1;
    private Product product2;
    private String randomCode;
    private String randomName;
    private BigDecimal randomPrice;

    @BeforeEach
    void setUp() {
        faker = TestDataFactory.getFaker();
        product1 = TestDataFactory.createProduct();
        product2 = TestDataFactory.createProduct();

        randomCode = TestDataFactory.generateProductCode();
        randomName = TestDataFactory.generateProductName();
        randomPrice = TestDataFactory.generateBasePrice();
    }

    @Test
    @DisplayName("Should return all products")
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<Product> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(2, products.size());
        verify(productRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Should return product by code")
    void testGetProductByCode() {
        when(productRepository.findByCode(product1.getCode())).thenReturn(Optional.of(product1));

        Optional<Product> result = productService.getProductByCode(product1.getCode());

        assertTrue(result.isPresent());
        assertEquals(product1.getCode(), result.get().getCode());
        assertEquals(product1.getName(), result.get().getName());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent product")
    void testGetProductByCodeNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductByCode(nonExistentCode);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should create a new product successfully")
    void testCreateProduct() {
        ProductRequest request = TestDataFactory.createProductRequest(randomCode, randomName, randomPrice);

        when(productRepository.existsByCode(randomCode)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals(randomCode, result.getCode());
        assertEquals(randomName, result.getName());
        assertEquals(randomPrice, result.getBasePrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate product")
    void testCreateProductDuplicate() {
        ProductRequest request = TestDataFactory.createProductRequest(
                product1.getCode(), product1.getName(), product1.getBasePrice());

        when(productRepository.existsByCode(product1.getCode())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> productService.createProduct(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update an existing product")
    void testUpdateProduct() {
        String updatedName = faker.food().ingredient();
        BigDecimal updatedPrice = TestDataFactory.generateBasePrice();

        ProductRequest request = TestDataFactory.createProductRequest(
                product1.getCode(), updatedName, updatedPrice);

        when(productRepository.findByCode(product1.getCode())).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(product1.getCode(), request);

        assertNotNull(result);
        assertEquals(product1.getCode(), result.getCode());
        assertEquals(updatedName, result.getName());
        assertEquals(updatedPrice, result.getBasePrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void testUpdateProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        ProductRequest request = TestDataFactory.createProductRequest();

        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(nonExistentCode, request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete an existing product")
    void testDeleteProduct() {
        when(productRepository.existsByCode(product1.getCode())).thenReturn(true);
        doNothing().when(productRepository).deleteByCode(product1.getCode());

        assertDoesNotThrow(() -> productService.deleteProduct(product1.getCode()));

        verify(productRepository, times(1)).deleteByCode(product1.getCode());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        when(productRepository.existsByCode(nonExistentCode)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(nonExistentCode));

        verify(productRepository, never()).deleteByCode(anyString());
    }

}
