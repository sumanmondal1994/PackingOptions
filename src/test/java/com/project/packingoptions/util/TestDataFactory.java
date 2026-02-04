package com.project.packingoptions.util;

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

}
