package com.one.core.application.dto.tenant.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseReportRow(
        LocalDate date,
        Long orderId,
        String supplier,
        String product,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost
) {}