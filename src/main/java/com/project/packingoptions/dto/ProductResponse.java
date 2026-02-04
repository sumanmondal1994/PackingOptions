package com.project.packingoptions.dto;

import com.project.packingoptions.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String code;
    private String name;
    private BigDecimal basePrice;

    public static ProductResponse fromProduct(Product product) {
        return ProductResponse.builder()
                .code(product.getCode())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .build();
    }
}
