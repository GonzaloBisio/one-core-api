package com.one.core.application.dto.tenant.purchases;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}