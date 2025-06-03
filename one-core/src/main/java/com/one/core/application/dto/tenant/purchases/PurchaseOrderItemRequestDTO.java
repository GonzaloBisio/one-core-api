package com.one.core.application.dto.tenant.purchases;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemRequestDTO {
    @NotNull private Long productId;
    @NotNull @Positive(message = "Quantity ordered must be positive")
    private BigDecimal quantityOrdered;
    @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;
}