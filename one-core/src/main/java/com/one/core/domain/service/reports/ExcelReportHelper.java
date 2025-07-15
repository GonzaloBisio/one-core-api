package com.one.core.domain.service.reports;

import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import com.one.core.domain.model.tenant.purchases.PurchaseOrderItem;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelReportHelper {

    private XSSFWorkbook workbook;
    // --- Paleta de Estilos ---
    private CellStyle titleStyle;
    private CellStyle headerStyle;
    private CellStyle kpiLabelStyle;
    private CellStyle kpiValueGreenStyle;
    private CellStyle kpiValueRedStyle;
    private CellStyle currencyProfitStyle;
    private CellStyle currencyLossStyle;
    private CellStyle currencyStyle;
    private CellStyle dateStyle;
    private CellStyle defaultCellStyle;

    public ByteArrayInputStream createOperationalSummaryExcel(
            String reportTitle, List<SalesOrder> sales, List<PurchaseOrder> purchases,
            BigDecimal totalSales, BigDecimal totalCostOfGoodsSold, BigDecimal totalPurchases) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            this.workbook = new XSSFWorkbook();
            createStyles();

            // Pasamos el título dinámico a la hoja de resumen
            createSummarySheet(reportTitle, totalSales, totalCostOfGoodsSold, totalPurchases);

            createDetailedSheet("Detalle de Ventas", getSalesHeaders(), sales);
            createDetailedSheet("Detalle de Compras", getPurchasesHeaders(), purchases);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage());
        } finally {
            try {
                if (workbook != null) workbook.close();
            } catch (IOException e) { /* Silently close */ }
        }
    }

    private void createStyles() {
        DataFormat dataFormat = workbook.createDataFormat();
        String currencyFormatString = "$ #,##0.00";

        defaultCellStyle = createBaseCellStyle();
        dateStyle = createBaseCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd/mm/yyyy"));

        titleStyle = createBaseCellStyle();
        titleStyle.setFont(createFont(18, true, IndexedColors.DARK_TEAL.getIndex()));

        headerStyle = createBaseCellStyle();
        headerStyle.setFont(createFont(12, true, IndexedColors.WHITE.getIndex()));
        headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        kpiLabelStyle = createBaseCellStyle();
        kpiLabelStyle.setFont(createFont(10, false, IndexedColors.GREY_50_PERCENT.getIndex()));
        kpiLabelStyle.setAlignment(HorizontalAlignment.CENTER);
        kpiLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        kpiValueGreenStyle = createBaseCellStyle();
        kpiValueGreenStyle.setFont(createFont(22, true, IndexedColors.DARK_GREEN.getIndex()));
        kpiValueGreenStyle.setDataFormat(dataFormat.getFormat(currencyFormatString));
        kpiValueGreenStyle.setAlignment(HorizontalAlignment.CENTER);
        kpiValueGreenStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        kpiValueRedStyle = createBaseCellStyle();
        kpiValueRedStyle.setFont(createFont(22, true, IndexedColors.RED.getIndex()));
        kpiValueRedStyle.setDataFormat(dataFormat.getFormat(currencyFormatString));
        kpiValueRedStyle.setAlignment(HorizontalAlignment.CENTER);
        kpiValueRedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        currencyStyle = createBaseCellStyle();
        currencyStyle.setDataFormat(dataFormat.getFormat(currencyFormatString));

        currencyProfitStyle = createBaseCellStyle();
        currencyProfitStyle.setDataFormat(dataFormat.getFormat(currencyFormatString));
        currencyProfitStyle.setFont(createFont(11, false, IndexedColors.DARK_GREEN.getIndex()));

        currencyLossStyle = createBaseCellStyle();
        currencyLossStyle.setDataFormat(dataFormat.getFormat(currencyFormatString));
        currencyLossStyle.setFont(createFont(11, false, IndexedColors.RED.getIndex()));
    }

    // --- HOJA DE RESUMEN CON LÓGICA DE CELDAS CORREGIDA ---
    private void createSummarySheet(String reportTitle, BigDecimal totalSales, BigDecimal totalCostOfGoodsSold, BigDecimal totalPurchases) {
        XSSFSheet sheet = workbook.createSheet("Resumen");

        BigDecimal grossProfit = totalSales.subtract(totalCostOfGoodsSold);

        // --- Título dinámico ---
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(30);
        Cell titleCell = createCell(titleRow, 1, reportTitle, titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 6));

        createKpiCard(sheet, 3, 1, "Ingresos por Ventas", totalSales, kpiValueGreenStyle);
        createKpiCard(sheet, 3, 4, "Costo de Mercadería (COGS)", totalCostOfGoodsSold, kpiValueRedStyle);
        createKpiCard(sheet, 7, 1, "Ganancia Bruta", grossProfit, grossProfit.compareTo(BigDecimal.ZERO) >= 0 ? kpiValueGreenStyle : kpiValueRedStyle);
        createKpiCard(sheet, 7, 4, "Egresos por Compras", totalPurchases, kpiValueRedStyle);

        for(int i=0; i<8; i++) sheet.setColumnWidth(i, 4000);
    }

    private void createKpiCard(XSSFSheet sheet, int row, int col, String label, BigDecimal value, CellStyle valueStyle) {
        // --- LÓGICA SIMPLIFICADA Y CORREGIDA ---
        // Celda para la etiqueta
        Row labelRow = getOrCreateRow(sheet, row);
        createCell(labelRow, col, label, kpiLabelStyle);
        sheet.addMergedRegion(new CellRangeAddress(row, row, col, col + 1)); // Unir celdas para la etiqueta

        // Celda para el valor
        Row valueRow = getOrCreateRow(sheet, row + 1);
        valueRow.setHeightInPoints(30); // Aumentar altura para el número grande
        Cell valueCell = createCell(valueRow, col, value, valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(row + 1, row + 2, col, col + 1)); // Unir celdas para el valor

        // Aplicar bordes
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(new CellRangeAddress(row, row + 2, col, col + 1), BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex(), BorderExtent.ALL);
        pt.applyBorders(sheet);
    }

    private void createDetailedSheet(String sheetName, String[] headers, List<?> data) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.createFreezePane(0, 1);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }

        int rowIdx = 1;
        if (data != null && !data.isEmpty()) {
            if (data.get(0) instanceof SalesOrder) {
                for (SalesOrder order : (List<SalesOrder>) data) {
                    for (SalesOrderItem item : order.getItems()) createSalesRow(sheet.createRow(rowIdx++), order, item);
                }
            } else if (data.get(0) instanceof PurchaseOrder) {
                for (PurchaseOrder order : (List<PurchaseOrder>) data) {
                    for (PurchaseOrderItem item : order.getItems()) createPurchasesRow(sheet.createRow(rowIdx++), order, item);
                }
            }
        }

        if (rowIdx > 1) {
            Row totalRow = sheet.createRow(rowIdx);
            int lastColIndex = headers.length - 1;

            Cell totalLabelCell;
            if (sheetName.equals("Detalle de Ventas")) {
                totalLabelCell = createCell(totalRow, lastColIndex - 3, "TOTALES:", headerStyle);
                addFormulaToTotalRow(totalRow, lastColIndex, rowIdx, currencyProfitStyle);
                addFormulaToTotalRow(totalRow, lastColIndex - 1, rowIdx, currencyStyle);
                addFormulaToTotalRow(totalRow, lastColIndex - 2, rowIdx, currencyStyle);
            } else { // Compras
                totalLabelCell = createCell(totalRow, lastColIndex - 1, "TOTAL:", headerStyle);
                addFormulaToTotalRow(totalRow, lastColIndex, rowIdx, currencyStyle);
            }
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSalesRow(Row row, SalesOrder order, SalesOrderItem item) {
        String customerName = (order.getCustomer() != null) ? order.getCustomer().getName() : "Consumidor Final";
        String productName = (item.getProduct() != null) ? item.getProduct().getName() : "Producto desconocido";

        BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
        BigDecimal unitPrice = item.getUnitPriceAtSale() != null ? item.getUnitPriceAtSale() : BigDecimal.ZERO;
        BigDecimal itemCost = (item.getProduct() != null && item.getProduct().getPurchasePrice() != null)
                ? item.getProduct().getPurchasePrice()
                : BigDecimal.ZERO;

        BigDecimal totalItemCost = itemCost.multiply(quantity);
        BigDecimal totalItemSale = unitPrice.multiply(quantity);
        BigDecimal itemProfit = totalItemSale.subtract(totalItemCost);

        createCell(row, 0, order.getOrderDate(), dateStyle);
        createCell(row, 1, order.getId(), defaultCellStyle);
        createCell(row, 2, customerName, defaultCellStyle);
        createCell(row, 3, productName, defaultCellStyle);
        createCell(row, 4, quantity.doubleValue(), defaultCellStyle);
        createCell(row, 5, unitPrice, currencyStyle);
        createCell(row, 6, totalItemSale, currencyStyle);
        createCell(row, 7, totalItemCost, currencyStyle);
        createCell(row, 8, itemProfit, itemProfit.compareTo(BigDecimal.ZERO) >= 0 ? currencyProfitStyle : currencyLossStyle);
    }

    private void createPurchasesRow(Row row, PurchaseOrder order, PurchaseOrderItem item) {
        String supplierName = (order.getSupplier() != null) ? order.getSupplier().getName() : "Proveedor desconocido";
        String productName = (item.getProduct() != null) ? item.getProduct().getName() : "Producto desconocido";

        BigDecimal quantity = item.getQuantityOrdered() != null ? item.getQuantityOrdered() : BigDecimal.ZERO;
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal totalCost = quantity.multiply(unitPrice);

        createCell(row, 0, order.getOrderDate(), dateStyle);
        createCell(row, 1, order.getId(), defaultCellStyle);
        createCell(row, 2, supplierName, defaultCellStyle);
        createCell(row, 3, productName, defaultCellStyle);
        createCell(row, 4, quantity.doubleValue(), defaultCellStyle);
        createCell(row, 5, unitPrice, currencyStyle);
        createCell(row, 6, totalCost, currencyStyle);
    }



    private CellStyle createBaseCellStyle() { return workbook.createCellStyle(); }
    private XSSFFont createFont(int height, boolean isBold, short color) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) height);
        font.setBold(isBold);
        font.setColor(color);
        return font;
    }
    private Row getOrCreateRow(XSSFSheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        return (row == null) ? sheet.createRow(rowNum) : row;
    }

    private void addFormulaToTotalRow(Row totalRow, int colIndex, int lastDataRow, CellStyle style) {
        if (colIndex < 0) return;
        char colLetter = (char) ('A' + colIndex);
        String formula = String.format("SUM(%c2:%c%d)", colLetter, colLetter, lastDataRow);
        Cell formulaCell = createCell(totalRow, colIndex, null, style);
        formulaCell.setCellFormula(formula);
    }

    private Cell createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (style != null) {
            cell.setCellStyle(style);
        }

        if (value == null) {
            // Deja la celda en blanco si el valor es nulo
            return cell;
        }

        // --- LÓGICA MEJORADA ---
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof BigDecimal) {
            // Maneja BigDecimal explícitamente para máxima precisión
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue((LocalDate) value);
        }
        // Si es otro tipo de Number, lo convertimos a double como última opción.
        else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }

        return cell;
    }

    private String[] getSalesHeaders() { return new String[]{"Fecha", "ID Orden", "Cliente", "Producto", "Cantidad", "Precio Venta", "Total Venta", "Total Costo", "Ganancia"}; }
    private String[] getPurchasesHeaders() { return new String[]{"Fecha", "ID Orden", "Proveedor", "Producto", "Cantidad", "Costo Unitario", "Total Costo"}; }

}