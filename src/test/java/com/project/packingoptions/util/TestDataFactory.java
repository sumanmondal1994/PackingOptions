package com.project.packingoptions.util;

import com.project.packingoptions.model.Product;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

}
