package com.one.core.application.dto.tenant.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesOrderItemRequestDTO {
    @NotNull private Long productId;
    @NotNull @Positive private BigDecimal quantity;
    @NotNull @DecimalMin("0.0") private BigDecimal unitPrice; // Precio al que se vende este ítem
    private BigDecimal discountPerItem; // Opcional
}