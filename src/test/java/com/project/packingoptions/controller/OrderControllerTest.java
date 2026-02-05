package com.project.packingoptions.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;
import com.project.packingoptions.dto.OrderResponse.PackageBreakdown;
import com.project.packingoptions.dto.OrderResponse.ProductBreakdown;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderResponse sampleOrder;

    @BeforeEach
    void setUp() {
        PackageBreakdown packageBreakdown = new PackageBreakdown(
                5, 2, new BigDecimal("20.95"), new BigDecimal("41.90"),
                "2 packages of 5 items ($20.95 each)");

        ProductBreakdown productBreakdown = new ProductBreakdown(
                "CE", "Cheese", 10, new BigDecimal("41.90"),
                Collections.singletonList(packageBreakdown));

        sampleOrder = new OrderResponse(
                1L, LocalDateTime.now(), new BigDecimal("41.90"),
                2, // totalPackages
                Collections.singletonList(productBreakdown));
    }

    @Test
    @DisplayName("GET /api/v1/orders - Should return all orders")
    void testGetAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Collections.singletonList(sampleOrder));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(1)))
                .andExpect(jsonPath("$[0].totalPrice", is(41.90)));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should return order by ID")
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.totalPrice", is(41.90)))
                .andExpect(jsonPath("$.productBreakdowns", hasSize(1)))
                .andExpect(jsonPath("$.productBreakdowns[0].productCode", is("CE")))
                .andExpect(jsonPath("$.productBreakdowns[0].quantityOrdered", is(10)));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should return 404 for non-existent order")
    void testGetOrderByIdNotFound() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should create an order with optimal packaging")
    void testCreateOrder() throws Exception {
        OrderRequest request = new OrderRequest(Arrays.asList(
                new OrderItemRequest("CE", 10)));

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.totalPrice", is(41.90)))
                .andExpect(jsonPath("$.productBreakdowns[0].productCode", is("CE")))
                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleSize", is(5)))
                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleCount", is(2)));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 400 for empty order")
    void testCreateOrderEmptyItems() throws Exception {
        OrderRequest request = new OrderRequest(Collections.emptyList());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 404 for non-existent product")
    void testCreateOrderProductNotFound() throws Exception {
        OrderRequest request = new OrderRequest(Arrays.asList(
                new OrderItemRequest("XX", 5)));

        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product", "code", "XX"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Should delete an order")
    void testDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Should return 404 for non-existent order")
    void testDeleteOrderNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Order", "id", 999L))
                .when(orderService).deleteOrder(999L);

        mockMvc.perform(delete("/api/v1/orders/999"))
                .andExpect(status().isNotFound());
    }
}
