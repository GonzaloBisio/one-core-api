package com.one.core.application.dto.tenant.sales;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuickSaleItemRequestDTO {
    private Long productId;

    @NotNull private BigDecimal quantity;
    @NotNull private BigDecimal unitPrice;

    private QuickProductDTO quickProduct;
    private Boolean skipAutoPackaging = false;
}