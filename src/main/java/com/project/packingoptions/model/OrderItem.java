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

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_code", length = 10, nullable = false)
    private String productCode;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(name = "bundle_size", nullable = false)
    private int bundleSize;

    @Column(name = "bundle_count", nullable = false)
    private int bundleCount;

    @Column(name = "price_at_time", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtTime;


    public BigDecimal getTotalPrice() {
        return priceAtTime.multiply(BigDecimal.valueOf(bundleCount));
    }


    public static OrderItem of(String productCode, int quantityOrdered, int bundleSize,
                               int bundleCount, BigDecimal priceAtTime) {
        return OrderItem.builder()
                .productCode(productCode)
                .quantityOrdered(quantityOrdered)
                .bundleSize(bundleSize)
                .bundleCount(bundleCount)
                .priceAtTime(priceAtTime)
                .build();
    }
}
