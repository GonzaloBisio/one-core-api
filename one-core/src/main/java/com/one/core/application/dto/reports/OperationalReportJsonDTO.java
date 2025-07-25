package com.one.core.application.dto.reports;

import com.one.core.application.dto.tenant.reports.PurchaseReportRow;
import com.one.core.application.dto.tenant.reports.SalesReportRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationalReportJsonDTO {
    // KPIs globales del período completo
    private String reportTitle;
    private BigDecimal totalSales;
    private BigDecimal totalCostOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal totalPurchases;

    // Detalles paginados y filtrados
    private Page<SalesReportRow> salesDetails;
    private Page<PurchaseReportRow> purchasesDetails;
}