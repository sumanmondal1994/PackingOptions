package com.project.packingoptions.controller;

import com.project.packingoptions.dto.ProductRequest;
import com.project.packingoptions.dto.ProductResponse;
import com.project.packingoptions.dto.ProductUpdateRequest;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management APIs")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;

        @GetMapping
        @Operation(summary = "Get all products", description = "Retrieves a list of all available products")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products")
        public ResponseEntity<List<ProductResponse>> getAllProducts() {
                List<ProductResponse> products = productService.getAllProducts().stream()
                                .map(ProductResponse::fromProduct)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(products);
        }

        @GetMapping("/{code}")
        @Operation(summary = "Get product by code", description = "Retrieves a product by its unique code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product found"),
                        @ApiResponse(responseCode = "404", description = "Product not found")
        })
        public ResponseEntity<ProductResponse> getProductByCode(
                        @Parameter(description = "Product code", example = "CE") @PathVariable String code) {
                return productService.getProductByCode(code)
                                .map(ProductResponse::fromProduct)
                                .map(ResponseEntity::ok)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", code));
        }

        @PostMapping
        @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Product created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "409", description = "Product already exists")
        })
        public ResponseEntity<ProductResponse> createProduct(
                        @Valid @RequestBody ProductRequest request) {
                ProductResponse response = ProductResponse.fromProduct(
                                productService.createProduct(request));

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/{code}")
        @Operation(summary = "Update a product", description = "Updates an existing product with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Product not found")
        })
        public ResponseEntity<ProductResponse> updateProduct(
                        @Parameter(description = "Product code", example = "CE") @PathVariable String code,
                        @Valid @RequestBody ProductUpdateRequest request) {
                ProductResponse response = ProductResponse.fromProduct(
                                productService.updateProduct(code, request));

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{code}")
        @Operation(summary = "Delete a product", description = "Deletes a product by its unique code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Product not found")
        })
        public ResponseEntity<Void> deleteProduct(
                        @Parameter(description = "Product code", example = "CE") @PathVariable String code) {
                productService.deleteProduct(code);

                return ResponseEntity.noContent().build();
        }
}
