package com.project.packingoptions.service;


import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;
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
import com.project.packingoptions.util.TestDataFactory;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PackagingOptionRepository packagingOptionRepository;

    @Mock
    private PackagingCalculatorService packagingCalculatorService;

    private OrderServiceImpl orderService;

    private Faker faker;
    private Product product1;
    private Product product2;
    private Product product3;
    private List<PackagingOption> product1Options;
    private List<PackagingOption> product2Options;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, productRepository,
                packagingOptionRepository, packagingCalculatorService);

        faker = TestDataFactory.getFaker();

        product1 = TestDataFactory.createProduct();
        product2 = TestDataFactory.createProduct();
        product3 = TestDataFactory.createProduct();

        product1Options = TestDataFactory.createPackagingOptions(product1.getCode(), 3, 5);
        product2Options = TestDataFactory.createPackagingOptions(product2.getCode(), 2, 5, 8);
    }

    @Test
    @DisplayName("Should create order with single product")
    void testCreateOrderSingleProduct() {
        // Arrange
        int quantity = TestDataFactory.generateQuantity(5, 15);
        BigDecimal bundlePrice = TestDataFactory.generatePrice(15, 25);
        int bundleSize = 5;
        int bundleCount = quantity / bundleSize;
        BigDecimal totalPrice = bundlePrice.multiply(BigDecimal.valueOf(bundleCount));

        OrderRequest request = TestDataFactory.createOrderRequest(product1.getCode(), quantity);

        when(productRepository.findByCode(product1.getCode())).thenReturn(Optional.of(product1));
        when(packagingOptionRepository.findByProductCode(product1.getCode())).thenReturn(product1Options);

        PackagingBreakdown breakdown = PackagingBreakdown.builder()
                .packages(Arrays.asList(PackageCount.builder()
                        .bundleSize(bundleSize)
                        .count(bundleCount)
                        .pricePerBundle(bundlePrice)
                        .build()))
                .totalPrice(totalPrice)
                .totalPackageCount(bundleCount)
                .build();
        when(packagingCalculatorService.calculateOptimalPackaging(anyInt(), any(), any()))
                .thenReturn(breakdown);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(faker.number().randomNumber());
            return order;
        });

        OrderResponse response = orderService.createOrder(request);
        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals(totalPrice, response.getTotalPrice());
        assertEquals(1, response.getProductBreakdowns().size());

        OrderResponse.ProductBreakdown productBreakdown = response.getProductBreakdowns().get(0);
        assertEquals(product1.getCode(), productBreakdown.getProductCode());
    }

    @Test
    @DisplayName("Should create order with multiple products")
    void testCreateOrderMultipleProducts() {
        int qty1 = TestDataFactory.generateQuantity(5, 15);
        int qty2 = TestDataFactory.generateQuantity(10, 20);
        int qty3 = TestDataFactory.generateQuantity(1, 5);

        OrderRequest request = OrderRequest.builder()
                .items(Arrays.asList(
                        OrderItemRequest.builder().productCode(product1.getCode())
                                .quantity(qty1).build(),
                        OrderItemRequest.builder().productCode(product2.getCode())
                                .quantity(qty2).build(),
                        OrderItemRequest.builder().productCode(product3.getCode())
                                .quantity(qty3).build()))
                .build();

        when(productRepository.findByCode(product1.getCode())).thenReturn(Optional.of(product1));
        when(productRepository.findByCode(product2.getCode())).thenReturn(Optional.of(product2));
        when(productRepository.findByCode(product3.getCode())).thenReturn(Optional.of(product3));

        when(packagingOptionRepository.findByProductCode(product1.getCode())).thenReturn(product1Options);
        when(packagingOptionRepository.findByProductCode(product2.getCode())).thenReturn(product2Options);
        when(packagingOptionRepository.findByProductCode(product3.getCode()))
                .thenReturn(Collections.emptyList());

        BigDecimal price1 = TestDataFactory.generatePrice(30, 50);
        BigDecimal price2 = TestDataFactory.generatePrice(60, 90);
        BigDecimal price3 = TestDataFactory.generatePrice(20, 40);

        PackagingBreakdown breakdown1 = PackagingBreakdown.builder()
                .packages(Arrays.asList(PackageCount.builder().bundleSize(5).count(2)
                        .pricePerBundle(price1.divide(BigDecimal.valueOf(2))).build()))
                .totalPrice(price1)
                .totalPackageCount(2)
                .build();

        PackagingBreakdown breakdown2 = PackagingBreakdown.builder()
                .packages(Arrays.asList(
                        PackageCount.builder().bundleSize(8).count(1)
                                .pricePerBundle(TestDataFactory.generatePrice(35, 45))
                                .build(),
                        PackageCount.builder().bundleSize(5).count(1)
                                .pricePerBundle(TestDataFactory.generatePrice(25, 35))
                                .build()))
                .totalPrice(price2)
                .totalPackageCount(2)
                .build();

        PackagingBreakdown breakdown3 = PackagingBreakdown.builder()
                .packages(Arrays.asList(PackageCount.builder().bundleSize(1).count(qty3)
                        .pricePerBundle(product3.getBasePrice()).build()))
                .totalPrice(price3)
                .totalPackageCount(qty3)
                .build();

        when(packagingCalculatorService.calculateOptimalPackaging(eq(qty1), eq(product1), any()))
                .thenReturn(breakdown1);
        when(packagingCalculatorService.calculateOptimalPackaging(eq(qty2), eq(product2), any()))
                .thenReturn(breakdown2);
        when(packagingCalculatorService.calculateOptimalPackaging(eq(qty3), eq(product3), any()))
                .thenReturn(breakdown3);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(faker.number().randomNumber());
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.getOrderId());
        assertEquals(price1.add(price2).add(price3), response.getTotalPrice());
        assertEquals(3, response.getProductBreakdowns().size());
    }

    @Test
    @DisplayName("Should throw exception for non-existent product")
    void testCreateOrderProductNotFound() {
        String nonExistentCode = TestDataFactory.generateProductCode();
        int randomQuantity = TestDataFactory.generateQuantity(1, 10);

        OrderRequest request = TestDataFactory.createOrderRequest(nonExistentCode, randomQuantity);

        when(productRepository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("Should get order by ID")
    void testGetOrderById() {
        Long orderId = faker.number().randomNumber();
        BigDecimal totalPrice = TestDataFactory.generatePrice(30, 100);
        int quantity = TestDataFactory.generateQuantity(5, 15);
        int bundleSize = 5;
        int bundleCount = quantity / bundleSize;
        BigDecimal bundlePrice = TestDataFactory.generatePrice(15, 25);

        Order order = Order.builder()
                .id(orderId)
                .createdAt(LocalDateTime.now())
                .totalPrice(totalPrice)
                .orderItems(new java.util.ArrayList<>())
                .build();
        OrderItem item = OrderItem.builder()
                .id(faker.number().randomNumber())
                .productCode(product1.getCode())
                .quantityOrdered(quantity)
                .bundleSize(bundleSize)
                .bundleCount(bundleCount)
                .priceAtTime(bundlePrice)
                .build();
        order.addOrderItem(item);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findByCode(product1.getCode())).thenReturn(Optional.of(product1));

        Optional<OrderResponse> result = orderService.getOrderById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getOrderId());
    }

    @Test
    @DisplayName("Should return empty for non-existent order")
    void testGetOrderByIdNotFound() {
        Long nonExistentId = faker.number().randomNumber();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<OrderResponse> result = orderService.getOrderById(nonExistentId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should delete order")
    void testDeleteOrder() {
        Long orderId = faker.number().randomNumber();
        when(orderRepository.existsById(orderId)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(orderId);

        assertDoesNotThrow(() -> orderService.deleteOrder(orderId));
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void testDeleteOrderNotFound() {
        Long nonExistentId = faker.number().randomNumber();
        when(orderRepository.existsById(nonExistentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(nonExistentId));
    }
}
