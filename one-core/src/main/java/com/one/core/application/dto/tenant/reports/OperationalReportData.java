package com.one.core.application.dto.tenant.reports;

import java.math.BigDecimal;
import java.util.List;

public record OperationalReportData(String reportTitle,
                                String generationDate,
                                BigDecimal totalSales,
                                BigDecimal totalCostOfGoodsSold,
                                BigDecimal grossProfit,
                                BigDecimal totalPurchases,
                                List<SalesReportRow> salesRows,
                                List<PurchaseReportRow> purchaseRows) {

}
