package com.project.packingoptions.service;

import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PackagingCalculatorServiceImpl implements PackagingCalculatorService {
    @Override
    public PackagingBreakdown calculateOptimalPackaging(int quantity, Product product, List<PackagingOption> packagingOptions) {
        log.debug("Calculating optimal packaging for {} units of {}", quantity, product.getCode());

        if (quantity <= 0) {
            return PackagingBreakdown.builder()
                    .packages(Collections.emptyList())
                    .totalPrice(BigDecimal.ZERO)
                    .totalPackageCount(0)
                    .build();
        }
        List<PackagingOption> sortedOptions = new ArrayList<>(packagingOptions);
        sortedOptions.sort((a, b) -> Integer.compare(b.getBundleSize(), a.getBundleSize()));

        List<PackageCount> packages = new ArrayList<>();
        int remainingQuantity = quantity;
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalPackageCount = 0;

        for (PackagingOption option : sortedOptions) {
            if (remainingQuantity >= option.getBundleSize()) {
                int bundleCount = remainingQuantity / option.getBundleSize();
                remainingQuantity = remainingQuantity % option.getBundleSize();

                packages.add(PackageCount.builder()
                        .bundleSize(option.getBundleSize())
                        .count(bundleCount)
                        .pricePerBundle(option.getBundlePrice())
                        .build());

                totalPrice = totalPrice.add(option.getBundlePrice().multiply(BigDecimal.valueOf(bundleCount)));
                totalPackageCount += bundleCount;

                log.debug("Using {} x {} bundle(s) @ {}", bundleCount, option.getBundleSize(),
                        option.getBundlePrice());
            }
        }

        if (remainingQuantity > 0) {
            packages.add(PackageCount.builder()
                    .bundleSize(1)  // Individual item
                    .count(remainingQuantity)
                    .pricePerBundle(product.getBasePrice())
                    .build());

            totalPrice = totalPrice.add(product.getBasePrice().multiply(BigDecimal.valueOf(remainingQuantity)));
            totalPackageCount += remainingQuantity;

            log.debug("Using {} x individual item(s) @ {}", remainingQuantity, product.getBasePrice());
        }

        log.info("Optimal packaging for {} {}: {} packages, total ${}",
                quantity, product.getCode(), totalPackageCount, totalPrice);

        return PackagingBreakdown.builder()
                .packages(packages)
                .totalPrice(totalPrice)
                .totalPackageCount(totalPackageCount)
                .build();
    }

}
