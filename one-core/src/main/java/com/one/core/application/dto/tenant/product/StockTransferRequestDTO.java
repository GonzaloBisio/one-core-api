package com.one.core.application.dto.tenant.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockTransferRequestDTO {
    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private BigDecimal quantity;
}