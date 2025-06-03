package com.one.core.application.dto.tenant.purchases;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodsReceiptItemDTO {
    @NotNull private Long purchaseOrderItemId; // ID del ítem de la orden de compra original
    @NotNull @Positive(message = "Quantity received must be positive")
    private BigDecimal quantityReceivedNow;
}