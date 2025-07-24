package com.one.core.domain.service.reports;

import com.one.core.application.dto.tenant.reports.OperationalReportData;
import com.one.core.application.dto.tenant.reports.PurchaseReportRow;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional(readOnly = true)
    public ByteArrayInputStream generateOperationalSummaryReport(String reportType, LocalDate date) {
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

        List<SalesOrder> sales = salesOrderRepository.findByOrderDateBetweenAndStatusNot(startDate, endDate, SalesOrderStatus.CANCELLED);
        List<PurchaseOrder> purchases = purchaseOrderRepository.findByOrderDateBetweenAndStatusNot(startDate, endDate, PurchaseOrderStatus.CANCELLED);

        BigDecimal totalSales = sales.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPurchases = purchases.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostOfGoodsSold = calculateCostOfGoodsSold(sales);
        BigDecimal grossProfit = totalSales.subtract(totalCostOfGoodsSold);

        List<SalesReportRow> salesRows = mapSalesToReportRows(sales);
        List<PurchaseReportRow> purchaseRows = mapPurchasesToReportRows(purchases);

        // CORRECCIÓN FINAL: Usar LocalDateTime aquí para la fecha de generación.
        String generationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        OperationalReportData reportData = new OperationalReportData(
                reportTitle,
                generationDate,
                totalSales, totalCostOfGoodsSold, grossProfit, totalPurchases,
                salesRows, purchaseRows
        );

        try {
            return excelReportGenerator.generate(reportData);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte Excel.", e);
        }
    }

    private List<SalesReportRow> mapSalesToReportRows(List<SalesOrder> sales) {
        return sales.stream()
                .flatMap(order -> order.getItems().stream().map(item -> {
                    BigDecimal quantity = item.getQuantity();
                    BigDecimal unitPrice = item.getUnitPriceAtSale();
                    BigDecimal totalSale = quantity.multiply(unitPrice);
                    BigDecimal itemCost = calculateSingleItemCost(item.getProduct());
                    BigDecimal totalCost = itemCost.multiply(quantity);
                    BigDecimal profit = totalSale.subtract(totalCost);
                    return new SalesReportRow(order.getOrderDate(), order.getId(), order.getCustomer() != null ? order.getCustomer().getName() : "N/A", item.getProduct().getName(), quantity, unitPrice, totalSale, totalCost, profit);
                })).collect(Collectors.toList());
    }

    private List<PurchaseReportRow> mapPurchasesToReportRows(List<PurchaseOrder> purchases) {
        return purchases.stream()
                .flatMap(order -> order.getItems().stream().map(item -> {
                    BigDecimal quantity = item.getQuantityOrdered();
                    BigDecimal unitCost = item.getUnitPrice();
                    return new PurchaseReportRow(order.getOrderDate(), order.getId(), order.getSupplier().getName(), item.getProduct().getName(), quantity, unitCost, quantity.multiply(unitCost));
                })).collect(Collectors.toList());
    }

    private BigDecimal calculateCostOfGoodsSold(List<SalesOrder> sales) {
        return sales.stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> calculateSingleItemCost(item.getProduct()).multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSingleItemCost(Product product) {
        if (product.getProductType() == ProductType.PHYSICAL_GOOD) {
            return product.getPurchasePrice() != null ? product.getPurchasePrice() : BigDecimal.ZERO;
        }
        if (product.getProductType() == ProductType.COMPOUND) {
            return productRecipeRepository.findByMainProductId(product.getId()).stream()
                    .map(recipe -> {
                        BigDecimal ingredientCost = recipe.getIngredientProduct().getPurchasePrice() != null ? recipe.getIngredientProduct().getPurchasePrice() : BigDecimal.ZERO;
                        return ingredientCost.multiply(recipe.getQuantityRequired());
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }
}