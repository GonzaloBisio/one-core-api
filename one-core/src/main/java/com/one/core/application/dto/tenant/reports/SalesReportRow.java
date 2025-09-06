package com.one.core.application.dto.tenant.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesReportRow(
        LocalDate date,
        Long orderId,
        String customer,
        String product,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalSale,
        BigDecimal totalCost,
        BigDecimal profit,
        String paymentMethod
) {}