package com.one.core.domain.service.reports;

import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.purchases.PurchaseOrderRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ExcelReportHelper excelReportHelper;

    // Nota: Esta constante no se usaba en el método. Se puede eliminar o usar de forma consistente.
    // private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Autowired
    public ReportService(SalesOrderRepository salesOrderRepository,
                         PurchaseOrderRepository purchaseOrderRepository,
                         ProductRecipeRepository productRecipeRepository,
                         ExcelReportHelper excelReportHelper) {
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.excelReportHelper = excelReportHelper;
    }

    /**
     * Genera un reporte de resumen operacional (diario, semanal o mensual).
     *
     * MODIFICADO: Este método ahora consulta las órdenes por su 'orderDate' (fecha de negocio)
     * en lugar de 'createdAt' (timestamp técnico), para asegurar que los reportes
     * reflejen la actividad del día correcto según la perspectiva del negocio.
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateOperationalSummaryReport(String reportType, LocalDate date) {
        // Se define la zona horaria del negocio para asegurar que los cálculos sean consistentes.
        final ZoneId businessZone = ZoneId.of("America/Argentina/Buenos_Aires");

        OffsetDateTime startDateTime;
        OffsetDateTime endDateTime;
        String reportTitle;

        // Lógica para determinar el rango de fechas y el título según el tipo,
        // siempre anclado a la zona horaria del negocio.
        switch (reportType.toUpperCase()) {
            case "DAILY":
                startDateTime = date.atStartOfDay(businessZone).toOffsetDateTime();
                endDateTime = date.plusDays(1).atStartOfDay(businessZone).toOffsetDateTime().minusNanos(1);
                reportTitle = "Resumen Diario: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                break;
            case "MONTHLY":
                YearMonth yearMonth = YearMonth.from(date);
                startDateTime = yearMonth.atDay(1).atStartOfDay(businessZone).toOffsetDateTime();
                endDateTime = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(businessZone).toOffsetDateTime().minusNanos(1);
                String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                reportTitle = "Resumen Mensual: " + monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + date.getYear();
                break;
            case "WEEKLY":
            default:
                startDateTime = date.with(DayOfWeek.MONDAY).atStartOfDay(businessZone).toOffsetDateTime();
                endDateTime = date.with(DayOfWeek.SUNDAY).plusDays(1).atStartOfDay(businessZone).toOffsetDateTime().minusNanos(1);
                reportTitle = "Resumen Semanal: " + startDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + endDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                break;
        }

        // Se usan los métodos de repositorio que aceptan OffsetDateTime para precisión.
        List<SalesOrder> sales = salesOrderRepository.findByDateTimeRangeAndStatusNot(startDateTime, endDateTime, SalesOrderStatus.CANCELLED);
        List<PurchaseOrder> purchases = purchaseOrderRepository.findByDateTimeRangeAndStatusNot(startDateTime, endDateTime, PurchaseOrderStatus.CANCELLED);

        // Los cálculos se centralizan aquí.
        BigDecimal totalSales = sales.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPurchases = purchases.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostOfGoodsSold = calculateCostOfGoodsSold(sales);

        // Se pasan los datos y cálculos al helper, que solo se encarga de la presentación.
        return excelReportHelper.createOperationalSummaryExcel(reportTitle, sales, purchases, totalSales, totalCostOfGoodsSold, totalPurchases);
    }

    /**
     * Calcula el Costo de Mercadería Vendida (COGS) para una lista de órdenes de venta.
     * Este método es "consciente de las recetas": sabe cómo calcular el costo
     * tanto para productos simples como para productos compuestos.
     */
    private BigDecimal calculateCostOfGoodsSold(List<SalesOrder> sales) {
        BigDecimal totalCogs = BigDecimal.ZERO;

        for (SalesOrder order : sales) {
            for (SalesOrderItem item : order.getItems()) {
                if (item.getProduct().getProductType() == ProductType.PHYSICAL_GOOD) {
                    BigDecimal cost = item.getProduct().getPurchasePrice() != null ? item.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                    totalCogs = totalCogs.add(cost.multiply(item.getQuantity()));

                } else if (item.getProduct().getProductType() == ProductType.COMPOUND) {
                    List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(item.getProduct().getId());
                    BigDecimal singleCompoundCost = BigDecimal.ZERO;

                    for (ProductRecipe recipeItem : recipeItems) {
                        BigDecimal ingredientCost = recipeItem.getIngredientProduct().getPurchasePrice() != null ? recipeItem.getIngredientProduct().getPurchasePrice() : BigDecimal.ZERO;
                        singleCompoundCost = singleCompoundCost.add(ingredientCost.multiply(recipeItem.getQuantityRequired()));
                    }
                    totalCogs = totalCogs.add(singleCompoundCost.multiply(item.getQuantity()));
                }
            }
        }
        return totalCogs;
    }
}