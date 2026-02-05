package com.project.packingoptions.service;

import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.service.PackagingCalculatorService.PackageCount;
import com.project.packingoptions.service.PackagingCalculatorService.PackagingBreakdown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PackagingCalculatorServiceTest {
    private PackagingCalculatorService calculatorService;
    private Product cheese;
    private Product ham;
    private Product soySauce;
    private List<PackagingOption> cheeseOptions;
    private List<PackagingOption> hamOptions;

    @BeforeEach
    void setUp() {
        calculatorService = new PackagingCalculatorServiceImpl();
        cheese = Product.builder()
                .code("CE")
                .name("Cheese")
                .basePrice(new BigDecimal("5.95"))
                .build();
        ham = Product.builder()
                .code("HM")
                .name("Ham")
                .basePrice(new BigDecimal("7.95"))
                .build();
        soySauce = Product.builder()
                .code("SS")
                .name("Soy Sauce")
                .basePrice(new BigDecimal("11.95"))
                .build();

        // Set up packaging options for Cheese (3 for $14.95, 5 for $20.95)
        cheeseOptions = Arrays.asList(
                PackagingOption.builder().id(1L).productCode("CE").bundleSize(3).bundlePrice(new BigDecimal("14.95")).build(),
                PackagingOption.builder().id(2L).productCode("CE").bundleSize(5).bundlePrice(new BigDecimal("20.95")).build()
        );

        // Set up packaging options for Ham (2 for $13.95, 5 for $29.95, 8 for $40.95)
        hamOptions = Arrays.asList(
                PackagingOption.builder().id(3L).productCode("HM").bundleSize(2).bundlePrice(new BigDecimal("13.95")).build(),
                PackagingOption.builder().id(4L).productCode("HM").bundleSize(5).bundlePrice(new BigDecimal("29.95")).build(),
                PackagingOption.builder().id(5L).productCode("HM").bundleSize(8).bundlePrice(new BigDecimal("40.95")).build()
        );
    }

    @Test
    @DisplayName("Should calculate optimal packaging for 10 Cheese (2x5 bundles)")
    void testOptimalPackagingFor10Cheese() {
         PackagingBreakdown result = calculatorService.calculateOptimalPackaging(10, cheese, cheeseOptions);

        assertNotNull(result);
        assertEquals(new BigDecimal("41.90"), result.getTotalPrice());
        assertEquals(2, result.getTotalPackageCount());
        List<PackageCount> packages = result.getPackages();
        assertEquals(1, packages.size());
        assertEquals(5, packages.get(0).getBundleSize());
        assertEquals(2, packages.get(0).getCount());
    }

    @Test
    @DisplayName("Should calculate optimal packaging for 14 Ham (1x8 + 1x5 + 1x1)")
    void testOptimalPackagingFor14Ham() {
        // Given: 14 HM
        // Expected: 1 package of 8 ($40.95) + 1 package of 5 ($29.95) + 1 individual ($7.95) = $78.85

        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(14, ham, hamOptions);

        assertNotNull(result);
        assertEquals(new BigDecimal("78.85"), result.getTotalPrice());
        assertEquals(3, result.getTotalPackageCount());
    }

    @Test
    @DisplayName("Should use individual items when no bundles available")
    void testPackagingWithNoBundles() {

        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(
                3, soySauce, Collections.emptyList());

        assertNotNull(result);
        assertEquals(new BigDecimal("35.85"), result.getTotalPrice());
        assertEquals(3, result.getTotalPackageCount());
        List<PackageCount> packages = result.getPackages();
        assertEquals(1, packages.size());
        assertEquals(1, packages.get(0).getBundleSize());
        assertEquals(3, packages.get(0).getCount());
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(0, cheese, cheeseOptions);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertEquals(0, result.getTotalPackageCount());
        assertTrue(result.getPackages().isEmpty());
    }

    @Test
    @DisplayName("Should handle quantity less than smallest bundle")
    void testQuantityLessThanSmallestBundle() {
        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(2, cheese, cheeseOptions);

        assertNotNull(result);
        assertEquals(new BigDecimal("11.90"), result.getTotalPrice());
        assertEquals(2, result.getTotalPackageCount());
    }

    @Test
    @DisplayName("Should minimize packages for 7 Cheese")
    void testOptimalPackagingFor7Cheese() {
        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(7, cheese, cheeseOptions);
        assertNotNull(result);
        assertEquals(new BigDecimal("32.85"), result.getTotalPrice());
        assertEquals(3, result.getTotalPackageCount());
    }

    @Test
    @DisplayName("Should handle exact bundle match")
    void testExactBundleMatch() {
        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(5, cheese, cheeseOptions);

        assertNotNull(result);
        assertEquals(new BigDecimal("20.95"), result.getTotalPrice());
        assertEquals(1, result.getTotalPackageCount());
    }

    @Test
    @DisplayName("Should handle large quantity")
    void testLargeQuantity() {

        PackagingBreakdown result = calculatorService.calculateOptimalPackaging(25, cheese, cheeseOptions);

        assertNotNull(result);
        assertEquals(new BigDecimal("104.75"), result.getTotalPrice());
        assertEquals(5, result.getTotalPackageCount());
    }
}
