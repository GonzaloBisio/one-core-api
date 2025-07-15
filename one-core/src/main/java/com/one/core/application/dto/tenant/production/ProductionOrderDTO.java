package com.one.core.application.dto.tenant.production;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class ProductionOrderDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantityProduced;
    private LocalDate productionDate;
    private String notes;
    private Long createdByUserId;
    private String createdByUsername;
    private OffsetDateTime createdAt;
}