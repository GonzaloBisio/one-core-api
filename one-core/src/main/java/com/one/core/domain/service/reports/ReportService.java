package com.one.core.domain.service.reports;

import com.one.core.application.dto.tenant.reports.OperationalReportData;
import com.one.core.application.dto.reports.OperationalReportJsonDTO;
import com.one.core.application.dto.tenant.reports.PurchaseReportRow;
import com.one.core.application.dto.reports.ReportFilterDTO;
import com.one.core.application.dto.tenant.reports.SalesReportRow;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.purchases.PurchaseOrderRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import com.one.core.domain.service.reports.criteria.SalesOrderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ExcelReportGenerator excelReportGenerator;

    @Autowired
    public ReportService(SalesOrderRepository salesOrderRepository,
                         PurchaseOrderRepository purchaseOrderRepository,
                         ProductRecipeRepository productRecipeRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.excelReportGenerator = new ExcelReportGenerator();
    }

    private static BigDecimal n(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private static String s(String v) { return (v != null && !v.isBlank()) ? v : "N/A"; }


    /**
     * Genera el reporte completo en formato Excel.
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateOperationalSummaryReport(String reportType, LocalDate date) {
        ReportDataBundle dataBundle = gatherReportData(reportType, date);

        List<SalesReportRow> salesRows = mapSalesToReportRows(dataBundle.allSalesInPeriod());
        List<PurchaseReportRow> purchaseRows = mapPurchasesToReportRows(dataBundle.allPurchasesInPeriod());

        String generationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        OperationalReportData reportData = new OperationalReportData(
                dataBundle.reportTitle(),
                generationDate,
                dataBundle.totalSales(),
                dataBundle.totalCostOfGoodsSold(),
                dataBundle.grossProfit(),
                dataBundle.totalPurchases(),
                salesRows,
                purchaseRows
        );

        try {
            return excelReportGenerator.generate(reportData);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte Excel.", e);
        }
    }

    /**
     * Obtiene los datos del reporte en formato JSON, con filtros y paginación.
     */
    @Transactional(readOnly = true)
    public OperationalReportJsonDTO getOperationalSummaryJson(String reportType, LocalDate date, ReportFilterDTO filter, Pageable pageable) {
        ReportDataBundle dataBundle = gatherReportData(reportType, date);

        Page<SalesReportRow> salesPage = Page.empty(pageable);
        Page<PurchaseReportRow> purchasesPage = Page.empty(pageable);

        String transactionType = filter.getTransactionType();

        if (!"PURCHASES".equalsIgnoreCase(transactionType)) {
            Specification<SalesOrder> spec = SalesOrderSpecification.filterBy(filter)
                    .and((root, query, cb) -> cb.between(root.get("orderDate"), dataBundle.startDate(), dataBundle.endDate()))
                    .and((root, query, cb) -> cb.notEqual(root.get("status"), SalesOrderStatus.CANCELLED));

            Page<SalesOrder> salesOrderPage = salesOrderRepository.findAll(spec, pageable);

            // Mapeamos el contenido de la página de Órdenes a una lista de Filas de Reporte (una orden puede generar varias filas)
            List<SalesReportRow> salesReportRows = mapSalesToReportRows(salesOrderPage.getContent());
            salesPage = new PageImpl<>(salesReportRows, pageable, salesOrderPage.getTotalElements());
        }

        if (!"SALES".equalsIgnoreCase(transactionType)) {
            Specification<PurchaseOrder> spec = (root, query, cb) -> cb.between(root.get("orderDate"), dataBundle.startDate(), dataBundle.endDate());
            // Se podría añadir PurchaseOrderSpecification aquí si se necesitan más filtros

            Page<PurchaseOrder> purchaseOrderPage = purchaseOrderRepository.findAll(spec, pageable);
            List<PurchaseReportRow> purchaseReportRows = mapPurchasesToReportRows(purchaseOrderPage.getContent());
            purchasesPage = new PageImpl<>(purchaseReportRows, pageable, purchaseOrderPage.getTotalElements());
        }

        return new OperationalReportJsonDTO(
                dataBundle.reportTitle(),
                dataBundle.totalSales(),
                dataBundle.totalCostOfGoodsSold(),
                dataBundle.grossProfit(),
                dataBundle.totalPurchases(),
                salesPage,
                purchasesPage
        );
    }

    /**
     * Método privado para centralizar la obtención de datos y cálculo de KPIs globales.
     * Es utilizado por ambos endpoints (Excel y JSON).
     */
    private ReportDataBundle gatherReportData(String reportType, LocalDate date) {
        LocalDate startDate;
        LocalDate endDate;
        String reportTitle;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Locale spanishLocale = new Locale("es", "ES");

        switch (reportType.toUpperCase()) {
            case "DAILY":
                startDate = date;
                endDate = date;
                reportTitle = "Resumen Diario: " + date.format(dateFormatter);
                break;
            case "WEEKLY":
                startDate = date.with(DayOfWeek.MONDAY);
                endDate = date.with(DayOfWeek.SUNDAY);
                reportTitle = "Resumen Semanal: " + startDate.format(dateFormatter) + " al " + endDate.format(dateFormatter);
                break;
            case "MONTHLY":
            default:
                startDate = date.with(TemporalAdjusters.firstDayOfMonth());
                endDate = date.with(TemporalAdjusters.lastDayOfMonth());
                String monthName = date.getMonth().getDisplayName(TextStyle.FULL, spanishLocale);
                reportTitle = "Resumen Mensual: " + monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + date.getYear();
                break;
        }

        List<SalesOrder> allSalesInPeriod = salesOrderRepository.findByOrderDateBetweenAndStatusNot(startDate, endDate, SalesOrderStatus.CANCELLED);
        List<PurchaseOrder> allPurchasesInPeriod = purchaseOrderRepository.findByOrderDateBetweenAndStatusNot(startDate, endDate, PurchaseOrderStatus.CANCELLED);

        BigDecimal totalSales = allSalesInPeriod.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPurchases = allPurchasesInPeriod.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostOfGoodsSold = calculateCostOfGoodsSold(allSalesInPeriod);
        BigDecimal grossProfit = totalSales.subtract(totalCostOfGoodsSold);

        return new ReportDataBundle(reportTitle, startDate, endDate, allSalesInPeriod, allPurchasesInPeriod, totalSales, totalPurchases, totalCostOfGoodsSold, grossProfit);
    }


    private List<SalesReportRow> mapSalesToReportRows(List<SalesOrder> sales) {
        if (sales == null || sales.isEmpty()) return Collections.emptyList();

        return sales.stream()
                .flatMap(order -> order.getItems().stream().map(item -> {
                    BigDecimal qty       = n(item.getQuantity());
                    BigDecimal unitPrice = n(item.getUnitPriceAtSale());
                    BigDecimal totalSale = qty.multiply(unitPrice);

                    String customer = order.getCustomer() != null ? s(order.getCustomer().getName()) : "N/A";
                    String productName = (item.getProduct() != null) ? s(item.getProduct().getName()) : "N/A";

                    BigDecimal itemCost = calculateSingleItemCost(item.getProduct());
                    BigDecimal totalCost = itemCost.multiply(qty);
                    BigDecimal profit = totalSale.subtract(totalCost);

                    return new SalesReportRow(
                            order.getOrderDate(), order.getId(), customer,
                            productName, qty, unitPrice, totalSale, totalCost, profit
                    );
                }))
                .collect(Collectors.toList());
    }

    private List<PurchaseReportRow> mapPurchasesToReportRows(List<PurchaseOrder> purchases) {
        if (purchases == null || purchases.isEmpty()) return Collections.emptyList();

        return purchases.stream()
                .flatMap(order -> order.getItems().stream().map(item -> {
                    BigDecimal qty  = n(item.getQuantityOrdered());
                    BigDecimal cost = n(item.getUnitPrice());

                    String supplier = (order.getSupplier() != null) ? s(order.getSupplier().getName()) : "N/A";
                    String productName = (item.getProduct() != null) ? s(item.getProduct().getName()) : "N/A";

                    return new PurchaseReportRow(
                            order.getOrderDate(), order.getId(), supplier,
                            productName, qty, cost, qty.multiply(cost)
                    );
                }))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateCostOfGoodsSold(List<SalesOrder> sales) {
        return sales.stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> calculateSingleItemCost(item.getProduct()).multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSingleItemCost(Product product) {
        return calculateSingleItemCost(product, 0);
    }
    private BigDecimal calculateSingleItemCost(Product product, int depth) {
        if (product == null || depth > 5 || product.getProductType() == null) return BigDecimal.ZERO;

        switch (product.getProductType()) {
            case PHYSICAL_GOOD:
                return n(product.getPurchasePrice());
            case COMPOUND:
                return productRecipeRepository.findByMainProductId(product.getId()).stream()
                        .map(r -> {
                            BigDecimal ingredientUnitCost = calculateSingleItemCost(r.getIngredientProduct(), depth + 1);
                            BigDecimal qty = n(r.getQuantityRequired());  // <- evita NPE si hay cantidad NULL
                            return ingredientUnitCost.multiply(qty);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Record interno para agrupar los datos comunes calculados.
     */
    private record ReportDataBundle(
            String reportTitle,
            LocalDate startDate,
            LocalDate endDate,
            List<SalesOrder> allSalesInPeriod,
            List<PurchaseOrder> allPurchasesInPeriod,
            BigDecimal totalSales,
            BigDecimal totalPurchases,
            BigDecimal totalCostOfGoodsSold,
            BigDecimal grossProfit
    ) {}
}