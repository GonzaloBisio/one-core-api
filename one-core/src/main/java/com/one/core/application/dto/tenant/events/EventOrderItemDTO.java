package com.one.core.application.dto.tenant.events;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EventOrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
