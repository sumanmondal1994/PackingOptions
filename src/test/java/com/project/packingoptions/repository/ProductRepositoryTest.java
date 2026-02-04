package com.project.packingoptions.repository;

import com.project.packingoptions.model.Product;
import com.project.packingoptions.util.TestDataFactory;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = TestDataFactory.getFaker();
    }

    @Test
    @DisplayName("Should find all products")
    void testFindAll() {
        List<Product> products = productRepository.findAll();

        assertNotNull(products);
        assertTrue(products.size() >= 3); // CE, HM, SS from data.sql
    }



}
