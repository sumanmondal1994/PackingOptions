package com.project.packingoptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.packingoptions.controller.ProductController;
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

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
public class ProductControllerTest {

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
        // Generate unique codes for each test
        product1Code = TestDataFactory.generateProductCode();
        product2Code = TestDataFactory.generateProductCode();

        // Create products with generated data
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
}
