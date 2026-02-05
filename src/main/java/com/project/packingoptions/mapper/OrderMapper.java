package com.project.packingoptions.mapper;


import com.project.packingoptions.dto.OrderResponse;
import com.project.packingoptions.dto.OrderResponse.PackageBreakdown;
import com.project.packingoptions.dto.OrderResponse.ProductBreakdown;
import com.project.packingoptions.model.Order;
import com.project.packingoptions.model.OrderItem;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.ProductRepository;
import com.project.packingoptions.service.PackagingCalculatorService.PackageCount;
import com.project.packingoptions.service.PackagingCalculatorService.PackagingBreakdown;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component for converting between Order entities and DTOs.
 * Provides a clear trust boundary - only data from entities (database) flows to
 * responses.
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductRepository productRepository;

    /**
     * Converts an Order entity to OrderResponse DTO.
     * Data flows from trusted source (database entity) to response.
     *
     * @param order the order entity from database
     * @return the order response DTO with product breakdowns
     */
    public OrderResponse toResponse(Order order) {
        List<ProductBreakdown> productBreakdowns = new ArrayList<>();

        // Group order items by product code
        order.getOrderItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getProductCode))
                .forEach((productCode, items) -> {
                    Product product = productRepository.findByCode(productCode)
                            .orElse(createUnknownProduct(productCode));

                    ProductBreakdown breakdown = buildProductBreakdown(product, items);
                    productBreakdowns.add(breakdown);
                });

        int totalPackages = calculateTotalPackages(productBreakdowns);

        return OrderResponse.builder()
                .orderId(order.getId())
                .createdAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .totalPackages(totalPackages)
                .productBreakdowns(productBreakdowns)
                .build();
    }

    /**
     * Builds an OrderResponse for a newly created order.
     * Uses data from saved order entity and pre-computed breakdowns.
     *
     * @param savedOrder        the saved order entity
     * @param productBreakdowns the pre-computed product breakdowns
     * @return the order response DTO
     */
    public OrderResponse toResponse(Order savedOrder, List<ProductBreakdown> productBreakdowns) {
        int totalPackages = calculateTotalPackages(productBreakdowns);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .createdAt(savedOrder.getCreatedAt())
                .totalPrice(savedOrder.getTotalPrice())
                .totalPackages(totalPackages)
                .productBreakdowns(productBreakdowns)
                .build();
    }

    /**
     * Creates a ProductBreakdown from packaging calculation results.
     * Uses data from trusted sources (Product entity and calculated packaging).
     *
     * @param product   the product entity from database
     * @param quantity  the ordered quantity (validated)
     * @param packaging the calculated packaging breakdown
     * @return the product breakdown DTO
     */
    public ProductBreakdown toProductBreakdown(Product product, int quantity, PackagingBreakdown packaging) {
        List<PackageBreakdown> packageBreakdowns = packaging.getPackages().stream()
                .map(this::toPackageBreakdown)
                .toList();

        return ProductBreakdown.builder()
                .productCode(product.getCode()) // From entity, not user input
                .productName(product.getName()) // From entity, not user input
                .quantityOrdered(quantity)
                .subtotal(packaging.getTotalPrice())
                .packages(packageBreakdowns)
                .build();
    }

    /**
     * Converts a PackageCount to PackageBreakdown DTO.
     *
     * @param packageCount the package count from calculation
     * @return the package breakdown DTO
     */
    public PackageBreakdown toPackageBreakdown(PackageCount packageCount) {
        String description = formatPackageDescription(
                packageCount.getCount(),
                packageCount.getBundleSize(),
                packageCount.getPricePerBundle());

        return PackageBreakdown.builder()
                .bundleSize(packageCount.getBundleSize())
                .bundleCount(packageCount.getCount())
                .pricePerBundle(packageCount.getPricePerBundle())
                .totalPrice(packageCount.getTotalPrice())
                .description(description)
                .build();
    }

    private ProductBreakdown buildProductBreakdown(Product product, List<OrderItem> items) {
        List<PackageBreakdown> packageBreakdowns = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItem item : items) {
            BigDecimal itemTotal = item.getPriceAtTime()
                    .multiply(BigDecimal.valueOf(item.getBundleCount()));
            subtotal = subtotal.add(itemTotal);
            totalQuantity = item.getQuantityOrdered();

            String description = formatPackageDescription(
                    item.getBundleCount(),
                    item.getBundleSize(),
                    item.getPriceAtTime());

            packageBreakdowns.add(PackageBreakdown.builder()
                    .bundleSize(item.getBundleSize())
                    .bundleCount(item.getBundleCount())
                    .pricePerBundle(item.getPriceAtTime())
                    .totalPrice(itemTotal)
                    .description(description)
                    .build());
        }

        return ProductBreakdown.builder()
                .productCode(product.getCode()) // From entity
                .productName(product.getName()) // From entity
                .quantityOrdered(totalQuantity)
                .subtotal(subtotal)
                .packages(packageBreakdowns)
                .build();
    }

    private String formatPackageDescription(int count, int bundleSize, BigDecimal pricePerBundle) {
        return String.format("%d package%s of %d item%s ($%.2f each)",
                count,
                count > 1 ? "s" : "",
                bundleSize,
                bundleSize > 1 ? "s" : "",
                pricePerBundle);
    }

    private int calculateTotalPackages(List<ProductBreakdown> productBreakdowns) {
        return productBreakdowns.stream()
                .flatMap(pb -> pb.getPackages().stream())
                .mapToInt(PackageBreakdown::getBundleCount)
                .sum();
    }

    private Product createUnknownProduct(String productCode) {
        return Product.builder()
                .code(productCode)
                .name("Unknown")
                .basePrice(BigDecimal.ZERO)
                .build();
    }
}
