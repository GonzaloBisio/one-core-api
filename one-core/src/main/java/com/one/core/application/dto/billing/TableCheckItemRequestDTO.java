package com.one.core.application.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Request payload to add an item to a table check.
 */
@Data
public class TableCheckItemRequestDTO {
    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @DecimalMin("0.0")
    private BigDecimal unitPrice;

    private BigDecimal discountPerItem;
}

