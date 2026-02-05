package com.project.packingoptions.service;

import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;

import java.util.List;
import java.util.Optional;


import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderResponse.PackageBreakdown;
import com.project.packingoptions.dto.OrderResponse.ProductBreakdown;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.Order;
import com.project.packingoptions.model.OrderItem;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.OrderRepository;
import com.project.packingoptions.repository.PackagingOptionRepository;
import com.project.packingoptions.repository.ProductRepository;
import com.project.packingoptions.service.PackagingCalculatorService.PackageCount;
import com.project.packingoptions.service.PackagingCalculatorService.PackagingBreakdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PackagingOptionRepository packagingOptionRepository;
    private final PackagingCalculatorService packagingCalculatorService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Retrieving all orders");
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(convertToOrderResponse(order));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        log.info("Retrieving order with ID: {}", id);
        return orderRepository.findById(id).map(this::convertToOrderResponse);
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating new order with {} items", request.getItems().size());

        List<OrderItem> orderItems = new ArrayList<>();
        List<ProductBreakdown> productBreakdowns = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            String productCode = itemRequest.getProductCode();
            int quantity = itemRequest.getQuantity();

            Product product = productRepository.findByCode(productCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "code",
                            productCode));

            List<PackagingOption> packagingOptions = packagingOptionRepository
                    .findByProductCode(productCode);

            PackagingBreakdown packaging = packagingCalculatorService.calculateOptimalPackaging(
                    quantity, product, packagingOptions);

            List<PackageBreakdown> packageBreakdowns = new ArrayList<>();

            for (PackageCount packageCount : packaging.getPackages()) {
                OrderItem orderItem = OrderItem.builder()
                        .productCode(productCode)
                        .quantityOrdered(quantity)
                        .bundleSize(packageCount.getBundleSize())
                        .bundleCount(packageCount.getCount())
                        .priceAtTime(packageCount.getPricePerBundle())
                        .build();
                orderItems.add(orderItem);

                String description = String.format("%d package%s of %d item%s ($%.2f each)",
                        packageCount.getCount(),
                        packageCount.getCount() > 1 ? "s" : "",
                        packageCount.getBundleSize(),
                        packageCount.getBundleSize() > 1 ? "s" : "",
                        packageCount.getPricePerBundle());

                packageBreakdowns.add(PackageBreakdown.builder()
                        .bundleSize(packageCount.getBundleSize())
                        .bundleCount(packageCount.getCount())
                        .pricePerBundle(packageCount.getPricePerBundle())
                        .totalPrice(packageCount.getTotalPrice())
                        .description(description)
                        .build());
            }

            ProductBreakdown productBreakdown = ProductBreakdown.builder()
                    .productCode(productCode)
                    .productName(product.getName())
                    .quantityOrdered(quantity)
                    .subtotal(packaging.getTotalPrice())
                    .packages(packageBreakdowns)
                    .build();
            productBreakdowns.add(productBreakdown);

            totalPrice = totalPrice.add(packaging.getTotalPrice());
        }
        Order order = Order.of(totalPrice, orderItems);
        Order savedOrder = orderRepository.save(order);

        int totalPackages = productBreakdowns.stream()
                .flatMap(pb -> pb.getPackages().stream())
                .mapToInt(PackageBreakdown::getBundleCount)
                .sum();

        log.info("Order created with ID: {}, total: ${}, packages: {}", savedOrder.getId(), totalPrice,
                totalPackages);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .createdAt(savedOrder.getCreatedAt())
                .totalPrice(totalPrice)
                .totalPackages(totalPackages)
                .productBreakdowns(productBreakdowns)
                .build();
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        List<ProductBreakdown> productBreakdowns = new ArrayList<>();

        order.getOrderItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getProductCode))
                .forEach((productCode, items) -> {
                    Product product = productRepository.findByCode(productCode)
                            .orElse(Product.builder()
                                    .code(productCode)
                                    .name("Unknown")
                                    .basePrice(BigDecimal.ZERO)
                                    .build());

                    List<PackageBreakdown> packageBreakdowns = new ArrayList<>();
                    BigDecimal subtotal = BigDecimal.ZERO;
                    int totalQuantity = 0;

                    for (OrderItem item : items) {
                        BigDecimal itemTotal = item.getPriceAtTime()
                                .multiply(BigDecimal.valueOf(item.getBundleCount()));
                        subtotal = subtotal.add(itemTotal);
                        totalQuantity = item.getQuantityOrdered();

                        String description = String.format(
                                "%d package%s of %d item%s ($%.2f each)",
                                item.getBundleCount(),
                                item.getBundleCount() > 1 ? "s" : "",
                                item.getBundleSize(),
                                item.getBundleSize() > 1 ? "s" : "",
                                item.getPriceAtTime());

                        packageBreakdowns.add(PackageBreakdown.builder()
                                .bundleSize(item.getBundleSize())
                                .bundleCount(item.getBundleCount())
                                .pricePerBundle(item.getPriceAtTime())
                                .totalPrice(itemTotal)
                                .description(description)
                                .build());
                    }

                    productBreakdowns.add(ProductBreakdown.builder()
                            .productCode(productCode)
                            .productName(product.getName())
                            .quantityOrdered(totalQuantity)
                            .subtotal(subtotal)
                            .packages(packageBreakdowns)
                            .build());
                });

        int totalPackages = productBreakdowns.stream()
                .flatMap(pb -> pb.getPackages().stream())
                .mapToInt(PackageBreakdown::getBundleCount)
                .sum();

        return OrderResponse.builder()
                .orderId(order.getId())
                .createdAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .totalPackages(totalPackages)
                .productBreakdowns(productBreakdowns)
                .build();
    }
}
