package com.one.core.application.dto.tenant.sales;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderPackagingDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
}
