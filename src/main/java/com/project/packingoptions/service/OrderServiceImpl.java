package com.project.packingoptions.service;

import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;

import java.util.List;
import java.util.Optional;


import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderResponse.ProductBreakdown;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.mapper.OrderMapper;
import com.project.packingoptions.model.Order;
import com.project.packingoptions.model.OrderItem;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.OrderRepository;
import com.project.packingoptions.repository.PackagingOptionRepository;
import com.project.packingoptions.repository.ProductRepository;
import com.project.packingoptions.service.PackagingCalculatorService.PackagingBreakdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PackagingOptionRepository packagingOptionRepository;
    private final PackagingCalculatorService packagingCalculatorService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Retrieving all orders");
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        log.info("Retrieving order with ID: {}", id);
        return orderRepository.findById(id).map(orderMapper::toResponse);
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

            for (var packageCount : packaging.getPackages()) {
                OrderItem orderItem = OrderItem.builder()
                        .productCode(product.getCode())
                        .quantityOrdered(quantity)
                        .bundleSize(packageCount.getBundleSize())
                        .bundleCount(packageCount.getCount())
                        .priceAtTime(packageCount.getPricePerBundle())
                        .build();
                orderItems.add(orderItem);
            }

            ProductBreakdown productBreakdown = orderMapper.toProductBreakdown(product, quantity,
                    packaging);
            productBreakdowns.add(productBreakdown);

            totalPrice = totalPrice.add(packaging.getTotalPrice());
        }

        Order order = Order.of(totalPrice, orderItems);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created with ID: {}, total: ${}", savedOrder.getId(), totalPrice);

        return orderMapper.toResponse(savedOrder, productBreakdowns);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);
    }
}
