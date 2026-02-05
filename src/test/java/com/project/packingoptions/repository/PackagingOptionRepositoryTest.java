package com.project.packingoptions.repository;


import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.util.TestDataFactory;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PackagingOptionRepository using Spring Data JPA.
 * Uses DataFaker for generating test data.
 */
@DataJpaTest
@ActiveProfiles("test")
class PackagingOptionRepositoryTest {

    @Autowired
    private PackagingOptionRepository packagingOptionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = TestDataFactory.getFaker();
        // Reset the auto-increment sequence to avoid conflicts with pre-seeded data
        // (IDs 1-5)
        entityManager.getEntityManager()
                .createNativeQuery("ALTER TABLE packaging_options ALTER COLUMN id RESTART WITH 100")
                .executeUpdate();
    }

    @Test
    @DisplayName("Should find all packaging options")
    void testFindAll() {
        List<PackagingOption> options = packagingOptionRepository.findAll();

        assertNotNull(options);
        // Verify pre-seeded data exists
        assertTrue(options.size() >= 1);
    }

    @Test
    @DisplayName("Should find packaging option by ID")
    void testFindById() {
        // Get any existing packaging option
        List<PackagingOption> allOptions = packagingOptionRepository.findAll();
        assertFalse(allOptions.isEmpty(), "No pre-seeded packaging options found");

        PackagingOption existingOption = allOptions.get(0);
        Optional<PackagingOption> result = packagingOptionRepository.findById(existingOption.getId());

        assertTrue(result.isPresent());
        assertEquals(existingOption.getId(), result.get().getId());
        assertEquals(existingOption.getBundleSize(), result.get().getBundleSize());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent packaging option")
    void testFindByIdNotFound() {
        Long nonExistentId = 99999L;

        Optional<PackagingOption> result = packagingOptionRepository.findById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find packaging options by product code")
    void testFindByProductCode() {
        // Testing pre-seeded data - CE should have packaging options
        List<PackagingOption> options = packagingOptionRepository.findByProductCode("CE");

        assertNotNull(options);
        assertTrue(options.size() >= 1);
        options.forEach(option -> assertEquals("CE", option.getProductCode()));
    }

    @Test
    @DisplayName("Should return empty list for product with no packaging options")
    void testFindByProductCodeEmpty() {
        // Create a new product without packaging options
        String uniqueCode = TestDataFactory.generateProductCode();
        while (productRepository.existsByCode(uniqueCode)) {
            uniqueCode = TestDataFactory.generateProductCode();
        }

        Product newProduct = Product.builder()
                .code(uniqueCode)
                .name(TestDataFactory.generateProductName())
                .basePrice(TestDataFactory.generateBasePrice())
                .build();
        productRepository.save(newProduct);

        List<PackagingOption> options = packagingOptionRepository.findByProductCode(uniqueCode);

        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test
    @DisplayName("Should save a new packaging option")
    void testSave() {
        // Get an existing product
        Product product = productRepository.findByCode("CE")
                .orElseThrow(() -> new AssertionError("Pre-seeded product CE not found"));

        int bundleSize = TestDataFactory.generateBundleSize();
        BigDecimal bundlePrice = TestDataFactory.generatePrice(10.00, 100.00);

        PackagingOption newOption = PackagingOption.builder()
                .product(product)
                .bundleSize(bundleSize)
                .bundlePrice(bundlePrice)
                .build();

        PackagingOption savedOption = packagingOptionRepository.save(newOption);

        assertNotNull(savedOption);
        assertNotNull(savedOption.getId());
        assertEquals(bundleSize, savedOption.getBundleSize());
        assertEquals(bundlePrice, savedOption.getBundlePrice());
    }

    @Test
    @DisplayName("Should update an existing packaging option")
    void testUpdate() {
        // Get an existing packaging option
        List<PackagingOption> allOptions = packagingOptionRepository.findAll();
        assertFalse(allOptions.isEmpty(), "No pre-seeded packaging options found");

        PackagingOption existingOption = allOptions.get(0);
        Long originalId = existingOption.getId();
        int newBundleSize = 15;
        BigDecimal newBundlePrice = new BigDecimal("99.99");

        existingOption.setBundleSize(newBundleSize);
        existingOption.setBundlePrice(newBundlePrice);

        PackagingOption updatedOption = packagingOptionRepository.save(existingOption);

        assertEquals(originalId, updatedOption.getId());
        assertEquals(newBundleSize, updatedOption.getBundleSize());
        assertEquals(newBundlePrice, updatedOption.getBundlePrice());
    }

    @Test
    @DisplayName("Should delete a packaging option by ID")
    void testDeleteById() {
        // Create a new packaging option to delete
        Product product = productRepository.findByCode("CE")
                .orElseThrow(() -> new AssertionError("Pre-seeded product CE not found"));

        PackagingOption newOption = PackagingOption.builder()
                .product(product)
                .bundleSize(5)
                .bundlePrice(new BigDecimal("25.00"))
                .build();

        PackagingOption savedOption = packagingOptionRepository.save(newOption);
        Long savedId = savedOption.getId();

        // Verify it exists
        assertTrue(packagingOptionRepository.existsById(savedId));

        // Delete it
        packagingOptionRepository.deleteById(savedId);

        // Verify it's deleted
        assertFalse(packagingOptionRepository.existsById(savedId));
    }

    @Test
    @DisplayName("Should delete all packaging options by product code")
    void testDeleteByProductCode() {
        // Create a new product with packaging options
        String uniqueCode = TestDataFactory.generateProductCode();
        while (productRepository.existsByCode(uniqueCode)) {
            uniqueCode = TestDataFactory.generateProductCode();
        }

        Product newProduct = Product.builder()
                .code(uniqueCode)
                .name(TestDataFactory.generateProductName())
                .basePrice(TestDataFactory.generateBasePrice())
                .build();
        productRepository.save(newProduct);

        // Add packaging options
        PackagingOption option1 = PackagingOption.builder()
                .product(newProduct)
                .bundleSize(3)
                .bundlePrice(new BigDecimal("15.00"))
                .build();
        PackagingOption option2 = PackagingOption.builder()
                .product(newProduct)
                .bundleSize(6)
                .bundlePrice(new BigDecimal("28.00"))
                .build();

        packagingOptionRepository.save(option1);
        packagingOptionRepository.save(option2);

        // Verify options exist
        List<PackagingOption> options = packagingOptionRepository.findByProductCode(uniqueCode);
        assertEquals(2, options.size());

        // Delete by product code
        packagingOptionRepository.deleteByProductCode(uniqueCode);

        // Verify options are deleted
        List<PackagingOption> deletedOptions = packagingOptionRepository.findByProductCode(uniqueCode);
        assertTrue(deletedOptions.isEmpty());
    }

    @Test
    @DisplayName("Should check if packaging option exists by ID")
    void testExistsById() {
        List<PackagingOption> allOptions = packagingOptionRepository.findAll();
        assertFalse(allOptions.isEmpty(), "No pre-seeded packaging options found");

        PackagingOption existingOption = allOptions.get(0);

        assertTrue(packagingOptionRepository.existsById(existingOption.getId()));
        assertFalse(packagingOptionRepository.existsById(99999L));
    }

    @Test
    @DisplayName("Should correctly calculate price per item")
    void testPricePerItem() {
        // Create a packaging option with known values
        Product product = productRepository.findByCode("CE")
                .orElseThrow(() -> new AssertionError("Pre-seeded product CE not found"));

        PackagingOption option = PackagingOption.builder()
                .product(product)
                .bundleSize(10)
                .bundlePrice(new BigDecimal("50.00"))
                .build();

        PackagingOption savedOption = packagingOptionRepository.save(option);

        // Price per item should be 50.00 / 10 = 5.00
        BigDecimal expectedPricePerItem = new BigDecimal("5.0000");
        assertEquals(expectedPricePerItem, savedOption.getPricePerItem());
    }
}

