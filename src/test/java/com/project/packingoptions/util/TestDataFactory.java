package com.project.packingoptions.util;

import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestDataFactory {

    private static final Faker faker = new Faker();
    public static Faker getFaker() {
        return faker;
    }

    public static String generateProductCode() {
        return faker.letterify("??").toUpperCase();
    }

    public static String generateProductName() {
        return faker.food().ingredient();
    }
    public static BigDecimal generatePrice(double min, double max) {
        double price = faker.number().randomDouble(2, (long) min, (long) max);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
    public static BigDecimal generateBasePrice() {
        return generatePrice(1.00, 50.00);
    }

    public static Product createProduct() {
        return Product.builder()
                .code(generateProductCode())
                .name(generateProductName())
                .basePrice(generateBasePrice())
                .build();
    }
    public static Product createProduct(String code) {
        return Product.builder()
                .code(code)
                .name(generateProductName())
                .basePrice(generateBasePrice())
                .build();
    }

    public static Product createProduct(String code, String name) {
        return Product.builder()
                .code(code)
                .name(name)
                .basePrice(generateBasePrice())
                .build();
    }

    public static Product createProduct(String code, String name, BigDecimal basePrice) {
        return Product.builder()
                .code(code)
                .name(name)
                .basePrice(basePrice)
                .build();
    }
    public static ProductRequest createProductRequest(String code, String name, BigDecimal price) {
        return ProductRequest.builder()
                .code(code)
                .name(name)
                .basePrice(price)
                .build();
    }
    public static ProductRequest createProductRequest() {
        return ProductRequest.builder()
                .code(generateProductCode())
                .name(generateProductName())
                .basePrice(generateBasePrice())
                .build();
    }

    public static List<Product> createProducts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createProduct())
                .collect(Collectors.toList());
    }

    public static int generateBundleSize() {
        return faker.number().numberBetween(2, 11);
    }


    public static PackagingOption createPackagingOption(String productCode) {
        int bundleSize = generateBundleSize();
        BigDecimal bundlePrice = generatePrice(10.00, 100.00);

        return PackagingOption.builder()
                .id(faker.number().randomNumber())
                .productCode(productCode)
                .bundleSize(bundleSize)
                .bundlePrice(bundlePrice)
                .build();
    }


}
