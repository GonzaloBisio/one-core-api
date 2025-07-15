package com.one.core.application.dto.tenant.production;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionOrderRequestDTO {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Quantity produced is required.")
    @Positive(message = "Quantity produced must be positive.")
    private BigDecimal quantityProduced;

    private String notes;
}