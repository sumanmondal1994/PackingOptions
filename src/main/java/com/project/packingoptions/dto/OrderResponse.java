package com.project.packingoptions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private int totalPackages;
    private List<ProductBreakdown> productBreakdowns;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBreakdown {
        private String productCode;
        private String productName;
        private int quantityOrdered;
        private BigDecimal subtotal;
        private List<PackageBreakdown> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageBreakdown {
        private int bundleSize;
        private int bundleCount;
        private BigDecimal pricePerBundle;
        private BigDecimal totalPrice;
        private String description;
    }
}

