package com.project.packingoptions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "packaging_options")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "product")
public class PackagingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "product_code", length = 10, nullable = false, insertable = false, updatable = false)
    private String productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", referencedColumnName = "code", nullable = false)
    private Product product;

    @Column(name = "bundle_size", nullable = false)
    private int bundleSize;

    @Column(name = "bundle_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal bundlePrice;

    public BigDecimal getPricePerItem() {
        return bundlePrice.divide(BigDecimal.valueOf(bundleSize), 4, RoundingMode.HALF_UP);
    }

    public static PackagingOption of(String productCode, int bundleSize, BigDecimal bundlePrice) {
        return PackagingOption.builder()
                .productCode(productCode)
                .bundleSize(bundleSize)
                .bundlePrice(bundlePrice)
                .build();
    }
}
