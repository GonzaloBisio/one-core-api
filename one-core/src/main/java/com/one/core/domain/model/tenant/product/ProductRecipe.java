package com.one.core.domain.model.tenant.product;

import com.one.core.domain.model.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "product_recipes")
@Data
@NoArgsConstructor
public class ProductRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_product_id", nullable = false)
    private Product mainProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_product_id", nullable = false)
    private Product ingredientProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "quantity_required", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityRequired;
}