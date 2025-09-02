package com.one.core.application.dto.tenant.sales;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderPackagingRequestDTO {
    @NotNull private Long productId;
    @NotNull @Positive private BigDecimal quantity;
}
