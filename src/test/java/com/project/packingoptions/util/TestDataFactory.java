package com.project.packingoptions.util;

import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.model.Order;
import com.project.packingoptions.model.OrderItem;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public static PackagingOption createPackagingOption(Long id, String productCode,
                                                        int bundleSize, BigDecimal bundlePrice) {
        return PackagingOption.builder()
                .id(id)
                .productCode(productCode)
                .bundleSize(bundleSize)
                .bundlePrice(bundlePrice)
                .build();
    }


    public static List<PackagingOption> createPackagingOptions(String productCode, int... bundleSizes) {
        List<PackagingOption> options = new ArrayList<>();
        long id = 1L;
        for (int size : bundleSizes) {
            BigDecimal price = generatePrice(size * 3.0, size * 8.0);
            options.add(PackagingOption.builder()
                    .id(id++)
                    .productCode(productCode)
                    .bundleSize(size)
                    .bundlePrice(price)
                    .build());
        }
        return options;
    }

    public static int generateQuantity() {
        return faker.number().numberBetween(1, 101);
    }

    public static int generateQuantity(int min, int max) {
        return faker.number().numberBetween(min, max + 1);
    }

    public static OrderItemRequest createOrderItemRequest(String productCode, int quantity) {
        return OrderItemRequest.builder()
                .productCode(productCode)
                .quantity(quantity)
                .build();
    }
    public static OrderItemRequest createOrderItemRequest(String productCode) {
        return OrderItemRequest.builder()
                .productCode(productCode)
                .quantity(generateQuantity(1, 20))
                .build();
    }
    public static OrderRequest createOrderRequest(List<OrderItemRequest> items) {
        return OrderRequest.builder()
                .items(items)
                .build();
    }
    public static OrderRequest createOrderRequest(String productCode, int quantity) {
        return OrderRequest.builder()
                .items(List.of(createOrderItemRequest(productCode, quantity)))
                .build();
    }

    public static Order createOrder(Long id, BigDecimal totalPrice, List<OrderItem> items) {
        Order order = Order.builder()
                .id(id)
                .createdAt(LocalDateTime.now())
                .totalPrice(totalPrice)
                .orderItems(items)
                .build();
        return order;
    }

    public static OrderItem createOrderItem(String productCode, int quantity,
                                            int bundleSize, int bundleCount, BigDecimal priceAtTime) {
        return OrderItem.builder()
                .productCode(productCode)
                .quantityOrdered(quantity)
                .bundleSize(bundleSize)
                .bundleCount(bundleCount)
                .priceAtTime(priceAtTime)
                .build();
    }


}
