package com.one.core.domain.model.tenant.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "product_packaging")
@Data
@NoArgsConstructor
public class ProductPackaging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_product_id", nullable = false)
    private Product mainProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_product_id", nullable = false)
    private Product packagingProduct;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;
}