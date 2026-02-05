package com.project.packingoptions.controller;

import com.project.packingoptions.dto.PackagingOptionRequest;
import com.project.packingoptions.dto.PackagingOptionResponse;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.service.PackagingOptionService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/packaging-options")
@Tag(name = "Packaging Options", description = "Packaging/Bundle management APIs")
@RequiredArgsConstructor
public class PackagingOptionController {

    private final PackagingOptionService packagingOptionService;

    @GetMapping
    @Operation(summary = "Get all packaging options", description = "Retrieves all available packaging options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of packaging options")
    public ResponseEntity<List<PackagingOptionResponse>> getAllPackagingOptions() {
        List<PackagingOptionResponse> options = packagingOptionService.getAllPackagingOptions().stream()
                .map(PackagingOptionResponse::fromPackagingOption)
                .collect(Collectors.toList());

        return ResponseEntity.ok(options);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get packaging option by ID", description = "Retrieves a packaging option by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packaging option found"),
            @ApiResponse(responseCode = "404", description = "Packaging option not found")
    })
    public ResponseEntity<PackagingOptionResponse> getPackagingOptionById(
            @Parameter(description = "Packaging option ID")
            @PathVariable Long id) {
        return packagingOptionService.getPackagingOptionById(id)
                .map(PackagingOptionResponse::fromPackagingOption)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("PackagingOption", "id", id));
    }

    @GetMapping("/product/{productCode}")
    @Operation(summary = "Get packaging options by product",
            description = "Retrieves all packaging options for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved packaging options"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<PackagingOptionResponse>> getPackagingOptionsByProduct(
            @Parameter(description = "Product code", example = "CE")
            @PathVariable String productCode) {
        List<PackagingOptionResponse> options = packagingOptionService
                .getPackagingOptionsByProductCode(productCode).stream()
                .map(PackagingOptionResponse::fromPackagingOption)
                .collect(Collectors.toList());

        return ResponseEntity.ok(options);
    }

    @PostMapping
    @Operation(summary = "Create a packaging option",
            description = "Creates a new packaging option for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Packaging option created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<PackagingOptionResponse> createPackagingOption(
            @Valid @RequestBody PackagingOptionRequest request) {
        PackagingOptionResponse response = PackagingOptionResponse.fromPackagingOption(
                packagingOptionService.createPackagingOption(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a packaging option",
            description = "Updates an existing packaging option")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packaging option updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Packaging option or product not found")
    })
    public ResponseEntity<PackagingOptionResponse> updatePackagingOption(
            @Parameter(description = "Packaging option ID")
            @PathVariable Long id,
            @Valid @RequestBody PackagingOptionRequest request) {
        PackagingOptionResponse response = PackagingOptionResponse.fromPackagingOption(
                packagingOptionService.updatePackagingOption(id, request));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a packaging option",
            description = "Deletes a packaging option by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Packaging option deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Packaging option not found")
    })
    public ResponseEntity<Void> deletePackagingOption(
            @Parameter(description = "Packaging option ID")
            @PathVariable Long id) {
        packagingOptionService.deletePackagingOption(id);

        return ResponseEntity.noContent().build();
    }
}