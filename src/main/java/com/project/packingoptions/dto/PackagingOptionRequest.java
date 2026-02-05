package com.project.packingoptions.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagingOptionRequest {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotNull(message = "Bundle size is required")
    @Min(value = 2, message = "Bundle size must be at least 2")
    private Integer bundleSize;

    @NotNull(message = "Bundle price is required")
    @DecimalMin(value = "0.01", message = "Bundle price must be greater than 0")
    private BigDecimal bundlePrice;
}
