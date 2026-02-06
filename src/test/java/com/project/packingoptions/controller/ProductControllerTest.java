package com.project.packingoptions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.dto.ProductUpdateRequest;
import com.project.packingoptions.exception.ResourceAlreadyExistsException;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.service.ProductService;
import com.project.packingoptions.util.TestDataFactory;
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
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductService productService;

        private Product product1;
        private Product product2;
        private String product1Code;
        private String product2Code;

        @BeforeEach
        void setUp() {
                product1Code = TestDataFactory.generateProductCode();
                product2Code = TestDataFactory.generateProductCode();

                product1 = TestDataFactory.createProduct(product1Code, "Cheese", new BigDecimal("5.95"));
                product2 = TestDataFactory.createProduct(product2Code, "Ham", new BigDecimal("7.95"));
        }

        @Test
        @DisplayName("GET /api/v1/products - Should return all products")
        void testGetAllProducts() throws Exception {
                when(productService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

                mockMvc.perform(get("/api/v1/products"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].code", is(product1Code)))
                                .andExpect(jsonPath("$[0].name", is("Cheese")))
                                .andExpect(jsonPath("$[1].code", is(product2Code)));
        }

        @Test
        @DisplayName("GET /api/v1/products/{code} - Should return product by code")
        void testGetProductByCode() throws Exception {
                when(productService.getProductByCode(product1Code)).thenReturn(Optional.of(product1));

                mockMvc.perform(get("/api/v1/products/" + product1Code))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.code", is(product1Code)))
                                .andExpect(jsonPath("$.name", is("Cheese")))
                                .andExpect(jsonPath("$.basePrice", is(5.95)));
        }

        @Test
        @DisplayName("GET /api/v1/products/{code} - Should return 404 for non-existent product")
        void testGetProductByCodeNotFound() throws Exception {
                String nonExistentCode = TestDataFactory.generateProductCode();
                when(productService.getProductByCode(nonExistentCode)).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/v1/products/" + nonExistentCode))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/v1/products - Should create a new product")
        void testCreateProduct() throws Exception {
                String newCode = TestDataFactory.generateProductCode();
                String newName = TestDataFactory.generateProductName();
                BigDecimal newPrice = TestDataFactory.generateBasePrice();

                ProductRequest request = TestDataFactory.createProductRequest(newCode, newName, newPrice);
                Product newProduct = TestDataFactory.createProduct(newCode, newName, newPrice);

                when(productService.createProduct(any(ProductRequest.class))).thenReturn(newProduct);

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code", is(newCode)))
                                .andExpect(jsonPath("$.name", is(newName)))
                                .andExpect(jsonPath("$.basePrice", is(newPrice.doubleValue())));
        }

        @Test
        @DisplayName("POST /api/v1/products - Should return 400 for invalid input")
        void testCreateProductInvalidInput() throws Exception {
                ProductRequest request = new ProductRequest("", "", null);

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/products - Should return 409 for duplicate product")
        void testCreateProductDuplicate() throws Exception {
                ProductRequest request = TestDataFactory.createProductRequest(product1Code, "Cheese",
                                new BigDecimal("5.95"));

                when(productService.createProduct(any(ProductRequest.class)))
                                .thenThrow(new ResourceAlreadyExistsException("Product", "code", product1Code));

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT /api/v1/products/{code} - Should update an existing product")
        void testUpdateProduct() throws Exception {
                String updatedName = "Premium " + TestDataFactory.generateProductName();
                BigDecimal updatedPrice = new BigDecimal("6.95");

                ProductUpdateRequest request = TestDataFactory.createProductUpdateRequest(updatedName, updatedPrice);
                Product updatedProduct = TestDataFactory.createProduct(product1Code, updatedName, updatedPrice);

                when(productService.updateProduct(eq(product1Code), any(ProductUpdateRequest.class)))
                                .thenReturn(updatedProduct);

                mockMvc.perform(put("/api/v1/products/" + product1Code)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code", is(product1Code)))
                                .andExpect(jsonPath("$.name", is(updatedName)))
                                .andExpect(jsonPath("$.basePrice", is(6.95)));
        }

        @Test
        @DisplayName("PUT /api/v1/products/{code} - Should return 404 for non-existent product")
        void testUpdateProductNotFound() throws Exception {
                String nonExistentCode = TestDataFactory.generateProductCode();
                ProductUpdateRequest request = TestDataFactory.createProductUpdateRequest("Unknown",
                                new BigDecimal("9.99"));

                when(productService.updateProduct(eq(nonExistentCode), any(ProductUpdateRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Product", "code", nonExistentCode));
                mockMvc.perform(put("/api/v1/products/" + nonExistentCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/v1/products/{code} - Should delete a product")
        void testDeleteProduct() throws Exception {
                doNothing().when(productService).deleteProduct(product1Code);

                mockMvc.perform(delete("/api/v1/products/" + product1Code))
                                .andExpect(status().isNoContent());

                verify(productService, times(1)).deleteProduct(product1Code);
        }

        @Test
        @DisplayName("DELETE /api/v1/products/{code} - Should return 404 for non-existent product")
        void testDeleteProductNotFound() throws Exception {
                String nonExistentCode = TestDataFactory.generateProductCode();

                doThrow(new ResourceNotFoundException("Product", "code", nonExistentCode))
                                .when(productService).deleteProduct(nonExistentCode);

                mockMvc.perform(delete("/api/v1/products/" + nonExistentCode))
                                .andExpect(status().isNotFound());
        }
}
