package com.project.packingoptions.controller;


import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs with optimal packaging calculation")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders with their packaging breakdowns")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID",
            description = "Retrieves an order with its complete packaging breakdown")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID")
            @PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @PostMapping
    @Operation(summary = "Create a new order",
            description = "Creates a new order with optimal packaging to minimize number of packages. " +
                    "The system will calculate the best way to package products using available bundle options.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully with packaging breakdown"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "One or more products not found")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {

        OrderRequest sanitizedRequest = sanitizeOrderRequest(request);
        OrderResponse response = orderService.createOrder(sanitizedRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

    private OrderRequest sanitizeOrderRequest(OrderRequest request) {
        if (request == null || request.getItems() == null) {
            return request;
        }

        List<OrderItemRequest> normalizedItems = request.getItems().stream()
                .map(item -> OrderItemRequest.builder()
                        .productCode(item.getProductCode().trim().toUpperCase())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return OrderRequest.builder()
                .items(normalizedItems)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order", description = "Deletes an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID")
            @PathVariable Long id) {
        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }
}

