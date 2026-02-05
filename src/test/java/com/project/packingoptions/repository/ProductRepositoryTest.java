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

    @Test
    @DisplayName("Should find product by code")
    void testFindByCode() {
         Optional<Product> result = productRepository.findByCode("CE");
        assertTrue(result.isPresent());
        assertEquals("CE", result.get().getCode());
        assertEquals("Cheese", result.get().getName());
        assertEquals(new BigDecimal("5.95"), result.get().getBasePrice());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent product")
    void testFindByCodeNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
          while (productRepository.existsByCode(nonExistentCode)) {
            nonExistentCode = TestDataFactory.generateProductCode();
        }

        Optional<Product> result = productRepository.findByCode(nonExistentCode);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should check if product exists")
    void testExistsByCode() {
        assertTrue(productRepository.existsByCode("CE"));
        assertTrue(productRepository.existsByCode("HM"));

        String nonExistentCode = TestDataFactory.generateProductCode();
        while (productRepository.existsByCode(nonExistentCode)) {
            nonExistentCode = TestDataFactory.generateProductCode();
        }
        assertFalse(productRepository.existsByCode(nonExistentCode));
    }

    @Test
    @DisplayName("Should save a new product")
    void testSave() {
         String randomCode = TestDataFactory.generateProductCode();
         while (productRepository.existsByCode(randomCode)) {
            randomCode = TestDataFactory.generateProductCode();
        }
        String randomName = TestDataFactory.generateProductName();
        BigDecimal randomPrice = TestDataFactory.generateBasePrice();
        Product newProduct = Product.builder()
                .code(randomCode)
                .name(randomName)
                .basePrice(randomPrice)
                .build();

        Product saved = productRepository.save(newProduct);

        assertNotNull(saved);
        assertEquals(randomCode, saved.getCode());
        assertEquals(randomName, saved.getName());
        assertEquals(randomPrice, saved.getBasePrice());
        Optional<Product> retrieved = productRepository.findByCode(randomCode);
        assertTrue(retrieved.isPresent());
        assertEquals(randomName, retrieved.get().getName());
        assertEquals(randomPrice, retrieved.get().getBasePrice());
    }

    @Test
    @DisplayName("Should update an existing product")
    void testUpdate() {
         Product existing = productRepository.findByCode("CE").orElseThrow();
         String updatedName = faker.food().ingredient() + " Premium";
         BigDecimal updatedPrice = TestDataFactory.generateBasePrice();
         existing.setName(updatedName);
         existing.setBasePrice(updatedPrice);

        Product result = productRepository.save(existing);

        assertNotNull(result);
        assertEquals("CE", result.getCode());
        assertEquals(updatedName, result.getName());
        assertEquals(updatedPrice, result.getBasePrice());

        Optional<Product> retrieved = productRepository.findByCode("CE");
        assertTrue(retrieved.isPresent());
        assertEquals(updatedName, retrieved.get().getName());
        assertEquals(updatedPrice, retrieved.get().getBasePrice());
    }

    @Test
    @DisplayName("Should delete a product")
    void testDelete() {
        String randomCode = TestDataFactory.generateProductCode();
        while (productRepository.existsByCode(randomCode)) {
            randomCode = TestDataFactory.generateProductCode();
        }
        String randomName = TestDataFactory.generateProductName();
        BigDecimal randomPrice = TestDataFactory.generateBasePrice();

        Product newProduct = Product.builder()
                .code(randomCode)
                .name(randomName)
                .basePrice(randomPrice)
                .build();
        productRepository.save(newProduct);

        assertTrue(productRepository.existsByCode(randomCode));

        productRepository.deleteByCode(randomCode);

        assertFalse(productRepository.existsByCode(randomCode));
    }



}
