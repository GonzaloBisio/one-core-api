package com.one.core.application.dto.tenant.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request payload for consuming packaging stock.
 * Allows optionally linking the consumption to a specific product.
 */
@Data
public class PackagingConsumptionRequestDTO {

    @NotNull
    private Long packagingProductId;

    @NotNull
    @Positive
    private BigDecimal quantity;

    /**
     * Optional product to validate packaging against.
     */
    private Long productId;
}
