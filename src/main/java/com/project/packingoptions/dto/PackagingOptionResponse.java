package com.project.packingoptions.dto;

import com.project.packingoptions.model.PackagingOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagingOptionResponse {

    private Long id;
    private String productCode;
    private int bundleSize;
    private BigDecimal bundlePrice;

    public static PackagingOptionResponse fromPackagingOption(PackagingOption option) {
        return PackagingOptionResponse.builder()
                .id(option.getId())
                .productCode(option.getProductCode())
                .bundleSize(option.getBundleSize())
                .bundlePrice(option.getBundlePrice())
                .build();
    }
}
