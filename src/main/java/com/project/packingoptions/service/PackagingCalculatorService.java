package com.project.packingoptions.service;


import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
public interface PackagingCalculatorService {

    PackagingBreakdown calculateOptimalPackaging(int quantity, Product product,
                                                 List<PackagingOption> packagingOptions);

    @Getter
    @Builder
    @AllArgsConstructor
    class PackagingBreakdown {
        private final List<PackageCount> packages;
        private final BigDecimal totalPrice;
        private final int totalPackageCount;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    class PackageCount {
        private final int bundleSize;
        private final int count;
        private final BigDecimal pricePerBundle;

        public BigDecimal getTotalPrice() {
            return pricePerBundle.multiply(BigDecimal.valueOf(count));
        }
    }
}
