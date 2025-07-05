package com.one.core.application.dto.tenant.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRecipeDTO {
    private Long id;

    @NotNull(message = "Ingredient product ID is required")
    private Long ingredientProductId;

    private String ingredientProductName;
    private String ingredientProductSku;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantityRequired;
}