package com.project.packingoptions.service;

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


}
