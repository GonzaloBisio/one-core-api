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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ExcelReportHelper excelReportHelper;

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

    @Transactional(readOnly = true)
    public ByteArrayInputStream generateWeeklySummaryReport(LocalDate date) {
        // 1. Calcular el inicio y fin de la semana para la fecha dada
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(DayOfWeek.SUNDAY);

        // 2. Obtener los datos de la base de datos
        List<SalesOrder> sales = salesOrderRepository.findByOrderDateBetweenAndStatusNot(
                startOfWeek, endOfWeek, SalesOrderStatus.CANCELLED
        );
        List<PurchaseOrder> purchases = purchaseOrderRepository.findByOrderDateBetweenAndStatusNot(
                startOfWeek, endOfWeek, PurchaseOrderStatus.CANCELLED
        );

        // 3. Centralizar los cálculos de negocio aquí
        BigDecimal totalSales = sales.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPurchases = purchases.stream().map(PurchaseOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCostOfGoodsSold = calculateCostOfGoodsSold(sales);

        // 4. Llamar al helper pasando los valores ya calculados
        return excelReportHelper.createWeeklySummaryExcel(sales, purchases, totalSales, totalCostOfGoodsSold, totalPurchases, startOfWeek, endOfWeek);
    }

    /**
     * Calcula el Costo de Mercadería Vendida (COGS) para una lista de órdenes de venta.
     * Este método es "consciente de las recetas": sabe cómo calcular el costo
     * tanto para productos simples como para productos compuestos.
     *
     * @param sales La lista de órdenes de venta.
     * @return El costo total de los productos vendidos.
     */
    private BigDecimal calculateCostOfGoodsSold(List<SalesOrder> sales) {
        BigDecimal totalCogs = BigDecimal.ZERO;

        for (SalesOrder order : sales) {
            for (SalesOrderItem item : order.getItems()) {
                if (item.getProduct().getProductType() == ProductType.PHYSICAL_GOOD) {
                    // Para productos simples, el costo es su precio de compra actual
                    BigDecimal cost = item.getProduct().getPurchasePrice() != null ? item.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                    totalCogs = totalCogs.add(cost.multiply(item.getQuantity()));

                } else if (item.getProduct().getProductType() == ProductType.COMPOUND) {
                    // Para productos compuestos, sumamos el costo de sus ingredientes
                    List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(item.getProduct().getId());
                    BigDecimal singleCompoundCost = BigDecimal.ZERO;

                    for (ProductRecipe recipeItem : recipeItems) {
                        BigDecimal ingredientCost = recipeItem.getIngredientProduct().getPurchasePrice() != null ? recipeItem.getIngredientProduct().getPurchasePrice() : BigDecimal.ZERO;
                        singleCompoundCost = singleCompoundCost.add(ingredientCost.multiply(recipeItem.getQuantityRequired()));
                    }
                    totalCogs = totalCogs.add(singleCompoundCost.multiply(item.getQuantity()));
                }
                // Los productos de tipo SERVICE, SUBSCRIPTION, etc., no suman al costo.
            }
        }
        return totalCogs;
    }
}