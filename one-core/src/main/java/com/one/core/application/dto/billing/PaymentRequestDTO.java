package com.one.core.application.dto.billing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Request payload for registering a payment on an open check.
 */
@Data
public class PaymentRequestDTO {
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;
}

