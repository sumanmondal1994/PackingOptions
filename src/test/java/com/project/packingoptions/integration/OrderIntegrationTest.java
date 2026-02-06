package com.project.packingoptions.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.packingoptions.dto.OrderItemRequest;
import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.dto.ProductUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Integration test: Create order with 10 CE, 14 HM, 3 SS")
        void testCreateOrderWithExampleScenario() throws Exception {
                OrderRequest request = new OrderRequest(Arrays.asList(
                                new OrderItemRequest("CE", 10),
                                new OrderItemRequest("HM", 14),
                                new OrderItemRequest("SS", 3)));

                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.orderId").isNumber())
                                .andExpect(jsonPath("$.totalPrice").isNumber())
                                .andExpect(jsonPath("$.productBreakdowns", hasSize(3)));
        }

        @Test
        @DisplayName("Integration test: Full product CRUD flow")
        void testProductCrudFlow() throws Exception {
                ProductRequest createRequest = new ProductRequest("TB", "Test Butter", new BigDecimal("3.95"));

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code", is("TB")))
                                .andExpect(jsonPath("$.name", is("Test Butter")));

                mockMvc.perform(get("/api/v1/products/TB"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code", is("TB")));

                ProductUpdateRequest updateRequest = new ProductUpdateRequest("Premium Test Butter",
                                new BigDecimal("4.95"));

                mockMvc.perform(put("/api/v1/products/TB")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("Premium Test Butter")))
                                .andExpect(jsonPath("$.basePrice", is(4.95)));

                mockMvc.perform(delete("/api/v1/products/TB"))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/api/v1/products/TB"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Integration test: Order with 10 Cheese should have 2 packages of 5")
        void testOrderWith10Cheese() throws Exception {
                OrderRequest request = new OrderRequest(Arrays.asList(
                                new OrderItemRequest("CE", 10)));

                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.totalPrice", is(41.90)))
                                .andExpect(jsonPath("$.productBreakdowns[0].productCode", is("CE")))
                                .andExpect(jsonPath("$.productBreakdowns[0].quantityOrdered", is(10)))
                                .andExpect(jsonPath("$.productBreakdowns[0].subtotal", is(41.90)))
                                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleSize", is(5)))
                                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleCount", is(2)));
        }

        @Test
        @DisplayName("Integration test: Order with product without packaging options uses base price")
        void testOrderWithNoPackagingOptions() throws Exception {
                OrderRequest request = new OrderRequest(Arrays.asList(
                                new OrderItemRequest("SS", 3)));

                mockMvc.perform(post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.totalPrice", is(35.85)))
                                .andExpect(jsonPath("$.productBreakdowns[0].productCode", is("SS")))
                                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleSize", is(1)))
                                .andExpect(jsonPath("$.productBreakdowns[0].packages[0].bundleCount", is(3)));
        }

        @Test
        @DisplayName("Integration test: Get all products")
        void testGetAllProducts() throws Exception {
                mockMvc.perform(get("/api/v1/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                                .andExpect(jsonPath("$[?(@.code=='CE')]").exists())
                                .andExpect(jsonPath("$[?(@.code=='HM')]").exists())
                                .andExpect(jsonPath("$[?(@.code=='SS')]").exists());
        }

        @Test
        @DisplayName("Integration test: Get packaging options for product")
        void testGetPackagingOptionsForProduct() throws Exception {
                mockMvc.perform(get("/api/v1/packaging-options/product/CE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[?(@.bundleSize==3)]").exists())
                                .andExpect(jsonPath("$[?(@.bundleSize==5)]").exists());
        }
}
