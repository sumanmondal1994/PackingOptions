package com.project.packingoptions.service;


import com.project.packingoptions.dto.PackagingOptionRequest;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.PackagingOptionRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackagingOptionService using DataFaker for test data
 * generation.
 */
@ExtendWith(MockitoExtension.class)
class PackagingOptionServiceTest {

    @Mock
    private PackagingOptionRepository packagingOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PackagingOptionServiceImpl packagingOptionService;

    private Faker faker;
    private Product product;
    private PackagingOption packagingOption1;
    private PackagingOption packagingOption2;
    private String productCode;

    @BeforeEach
    void setUp() {
        faker = TestDataFactory.getFaker();

        // Generate random test data
        productCode = TestDataFactory.generateProductCode();
        product = TestDataFactory.createProduct(productCode);

        packagingOption1 = TestDataFactory.createPackagingOption(1L, productCode, 5, new BigDecimal("25.00"));
        packagingOption2 = TestDataFactory.createPackagingOption(2L, productCode, 10, new BigDecimal("45.00"));
    }

    // ==================== getAllPackagingOptions Tests ====================

    @Test
    @DisplayName("Should return all packaging options")
    void testGetAllPackagingOptions() {
        when(packagingOptionRepository.findAll()).thenReturn(Arrays.asList(packagingOption1, packagingOption2));

        List<PackagingOption> options = packagingOptionService.getAllPackagingOptions();

        assertNotNull(options);
        assertEquals(2, options.size());
        verify(packagingOptionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no packaging options exist")
    void testGetAllPackagingOptionsEmpty() {
        when(packagingOptionRepository.findAll()).thenReturn(Collections.emptyList());

        List<PackagingOption> options = packagingOptionService.getAllPackagingOptions();

        assertNotNull(options);
        assertTrue(options.isEmpty());
        verify(packagingOptionRepository, times(1)).findAll();
    }

    // ==================== getPackagingOptionById Tests ====================

    @Test
    @DisplayName("Should return packaging option by ID")
    void testGetPackagingOptionById() {
        when(packagingOptionRepository.findById(1L)).thenReturn(Optional.of(packagingOption1));

        Optional<PackagingOption> result = packagingOptionService.getPackagingOptionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(5, result.get().getBundleSize());
        verify(packagingOptionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional for non-existent packaging option")
    void testGetPackagingOptionByIdNotFound() {
        Long nonExistentId = 99999L;
        when(packagingOptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<PackagingOption> result = packagingOptionService.getPackagingOptionById(nonExistentId);

        assertFalse(result.isPresent());
        verify(packagingOptionRepository, times(1)).findById(nonExistentId);
    }

    // ==================== getPackagingOptionsByProductCode Tests
    // ====================

    @Test
    @DisplayName("Should return packaging options by product code")
    void testGetPackagingOptionsByProductCode() {
        when(productRepository.existsByCode(productCode)).thenReturn(true);
        when(packagingOptionRepository.findByProductCode(productCode))
                .thenReturn(Arrays.asList(packagingOption1, packagingOption2));

        List<PackagingOption> options = packagingOptionService.getPackagingOptionsByProductCode(productCode);

        assertNotNull(options);
        assertEquals(2, options.size());
        verify(productRepository, times(1)).existsByCode(productCode);
        verify(packagingOptionRepository, times(1)).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should return empty list when product has no packaging options")
    void testGetPackagingOptionsByProductCodeEmpty() {
        when(productRepository.existsByCode(productCode)).thenReturn(true);
        when(packagingOptionRepository.findByProductCode(productCode)).thenReturn(Collections.emptyList());

        List<PackagingOption> options = packagingOptionService.getPackagingOptionsByProductCode(productCode);

        assertNotNull(options);
        assertTrue(options.isEmpty());
        verify(packagingOptionRepository, times(1)).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should throw exception when product does not exist")
    void testGetPackagingOptionsByProductCodeProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        when(productRepository.existsByCode(nonExistentCode)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> packagingOptionService.getPackagingOptionsByProductCode(nonExistentCode));

        verify(productRepository, times(1)).existsByCode(nonExistentCode);
        verify(packagingOptionRepository, never()).findByProductCode(any());
    }

    // ==================== createPackagingOption Tests ====================

    @Test
    @DisplayName("Should create a new packaging option successfully")
    void testCreatePackagingOption() {
        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(5)
                .bundlePrice(new BigDecimal("25.00"))
                .build();

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> {
            PackagingOption savedOption = i.getArgument(0);
            savedOption.setId(1L);
            return savedOption;
        });

        PackagingOption result = packagingOptionService.createPackagingOption(request);

        assertNotNull(result);
        assertEquals(productCode, result.getProductCode());
        assertEquals(5, result.getBundleSize());
        assertEquals(new BigDecimal("25.00"), result.getBundlePrice());
        verify(productRepository, times(1)).findByCode(productCode);
        verify(packagingOptionRepository, times(1)).save(any(PackagingOption.class));
    }

    @Test
    @DisplayName("Should throw exception when creating packaging option for non-existent product")
    void testCreatePackagingOptionProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(nonExistentCode)
                .bundleSize(5)
                .bundlePrice(new BigDecimal("25.00"))
                .build();

        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> packagingOptionService.createPackagingOption(request));

        verify(productRepository, times(1)).findByCode(nonExistentCode);
        verify(packagingOptionRepository, never()).save(any(PackagingOption.class));
    }

    @Test
    @DisplayName("Should create packaging option with random generated data")
    void testCreatePackagingOptionWithRandomData() {
        int randomBundleSize = TestDataFactory.generateBundleSize();
        BigDecimal randomBundlePrice = TestDataFactory.generatePrice(10.00, 100.00);

        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(randomBundleSize)
                .bundlePrice(randomBundlePrice)
                .build();

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> {
            PackagingOption savedOption = i.getArgument(0);
            savedOption.setId(1L);
            return savedOption;
        });

        PackagingOption result = packagingOptionService.createPackagingOption(request);

        assertNotNull(result);
        assertEquals(randomBundleSize, result.getBundleSize());
        assertEquals(randomBundlePrice, result.getBundlePrice());
    }

    // ==================== updatePackagingOption Tests ====================

    @Test
    @DisplayName("Should update an existing packaging option")
    void testUpdatePackagingOption() {
        int updatedBundleSize = 8;
        BigDecimal updatedBundlePrice = new BigDecimal("40.00");

        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(updatedBundleSize)
                .bundlePrice(updatedBundlePrice)
                .build();

        when(packagingOptionRepository.findById(1L)).thenReturn(Optional.of(packagingOption1));
        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> i.getArgument(0));

        PackagingOption result = packagingOptionService.updatePackagingOption(1L, request);

        assertNotNull(result);
        assertEquals(updatedBundleSize, result.getBundleSize());
        assertEquals(updatedBundlePrice, result.getBundlePrice());
        verify(packagingOptionRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByCode(productCode);
        verify(packagingOptionRepository, times(1)).save(any(PackagingOption.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent packaging option")
    void testUpdatePackagingOptionNotFound() {
        Long nonExistentId = 99999L;
        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(5)
                .bundlePrice(new BigDecimal("25.00"))
                .build();

        when(packagingOptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> packagingOptionService.updatePackagingOption(nonExistentId, request));

        verify(packagingOptionRepository, times(1)).findById(nonExistentId);
        verify(packagingOptionRepository, never()).save(any(PackagingOption.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with non-existent product code")
    void testUpdatePackagingOptionProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(nonExistentCode)
                .bundleSize(5)
                .bundlePrice(new BigDecimal("25.00"))
                .build();

        when(packagingOptionRepository.findById(1L)).thenReturn(Optional.of(packagingOption1));
        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> packagingOptionService.updatePackagingOption(1L, request));

        verify(packagingOptionRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByCode(nonExistentCode);
        verify(packagingOptionRepository, never()).save(any(PackagingOption.class));
    }

    // ==================== deletePackagingOption Tests ====================

    @Test
    @DisplayName("Should delete a packaging option successfully")
    void testDeletePackagingOption() {
        when(packagingOptionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(packagingOptionRepository).deleteById(1L);

        assertDoesNotThrow(() -> packagingOptionService.deletePackagingOption(1L));

        verify(packagingOptionRepository, times(1)).existsById(1L);
        verify(packagingOptionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent packaging option")
    void testDeletePackagingOptionNotFound() {
        Long nonExistentId = 99999L;
        when(packagingOptionRepository.existsById(nonExistentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> packagingOptionService.deletePackagingOption(nonExistentId));

        verify(packagingOptionRepository, times(1)).existsById(nonExistentId);
        verify(packagingOptionRepository, never()).deleteById(any());
    }

    // ==================== Additional Edge Case Tests ====================

    @Test
    @DisplayName("Should handle large bundle size")
    void testCreatePackagingOptionWithLargeBundleSize() {
        int largeBundleSize = 100;
        BigDecimal bundlePrice = new BigDecimal("500.00");

        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(largeBundleSize)
                .bundlePrice(bundlePrice)
                .build();

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> {
            PackagingOption savedOption = i.getArgument(0);
            savedOption.setId(1L);
            return savedOption;
        });

        PackagingOption result = packagingOptionService.createPackagingOption(request);

        assertNotNull(result);
        assertEquals(largeBundleSize, result.getBundleSize());
    }

    @Test
    @DisplayName("Should handle minimum bundle size")
    void testCreatePackagingOptionWithMinimumBundleSize() {
        int minBundleSize = 2; // Minimum valid bundle size
        BigDecimal bundlePrice = new BigDecimal("10.00");

        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(minBundleSize)
                .bundlePrice(bundlePrice)
                .build();

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> {
            PackagingOption savedOption = i.getArgument(0);
            savedOption.setId(1L);
            return savedOption;
        });

        PackagingOption result = packagingOptionService.createPackagingOption(request);

        assertNotNull(result);
        assertEquals(minBundleSize, result.getBundleSize());
    }

    @Test
    @DisplayName("Should handle decimal bundle price with precision")
    void testCreatePackagingOptionWithPreciseBundlePrice() {
        BigDecimal preciseBundlePrice = new BigDecimal("29.99");

        PackagingOptionRequest request = PackagingOptionRequest.builder()
                .productCode(productCode)
                .bundleSize(5)
                .bundlePrice(preciseBundlePrice)
                .build();

        when(productRepository.findByCode(productCode)).thenReturn(Optional.of(product));
        when(packagingOptionRepository.save(any(PackagingOption.class))).thenAnswer(i -> {
            PackagingOption savedOption = i.getArgument(0);
            savedOption.setId(1L);
            return savedOption;
        });

        PackagingOption result = packagingOptionService.createPackagingOption(request);

        assertNotNull(result);
        assertEquals(preciseBundlePrice, result.getBundlePrice());
    }
}
