package com.one.core.domain.service.reports;

import com.one.core.application.dto.tenant.reports.OperationalReportData;
import com.one.core.application.dto.tenant.reports.PurchaseReportRow;
import com.one.core.application.dto.tenant.reports.SalesReportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExcelReportGenerator {

    private static final IndexedColors COLOR_PRIMARY_DARK = IndexedColors.DARK_BLUE;
    private static final IndexedColors COLOR_ACCENT_GREEN = IndexedColors.SEA_GREEN;
    private static final IndexedColors COLOR_ACCENT_RED = IndexedColors.RED;
    private static final IndexedColors COLOR_FONT_LIGHT = IndexedColors.WHITE;
    private static final IndexedColors COLOR_FONT_DARK = IndexedColors.BLACK;
    private static final IndexedColors COLOR_FONT_SUBTLE = IndexedColors.GREY_50_PERCENT;
    private static final String FONT_NAME = "Calibri";

    public ByteArrayInputStream generate(OperationalReportData data) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ReportStyles styles = new ReportStyles(workbook);

            createSummarySheet(workbook.createSheet("Resumen"), data, styles);
            createSalesDetailSheet(workbook.createSheet("Detalle de Ventas"), data.salesRows(), styles);
            createPurchasesDetailSheet(workbook.createSheet("Detalle de Compras"), data.purchaseRows(), styles);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void createSummarySheet(Sheet sheet, OperationalReportData data, ReportStyles styles) {
        sheet.setColumnWidth(0, 1000);
        sheet.setColumnWidth(1, 7000);
        sheet.setColumnWidth(2, 7000);
        sheet.setColumnWidth(3, 1000);
        sheet.setColumnWidth(4, 7000);
        sheet.setColumnWidth(5, 7000);

        createCell(sheet, 1, 1, data.reportTitle(), styles.title());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 5));
        createCell(sheet, 2, 5, "Generado: " + data.generationDate(), styles.subtle());

        createKpiCard(sheet, 4, 1, "INGRESOS TOTALES (VENTAS)", data.totalSales(), styles, styles.kpiValuePositive());
        createKpiCard(sheet, 4, 4, "COSTO DE MERCADERÍA VENDIDA", data.totalCostOfGoodsSold(), styles, styles.kpiValueNegative());
        createKpiCard(sheet, 8, 1, "GANANCIA BRUTA (VENTAS - COSTO)", data.grossProfit(), styles, data.grossProfit().compareTo(BigDecimal.ZERO) >= 0 ? styles.kpiValuePositive() : styles.kpiValueNegative());
        createKpiCard(sheet, 8, 4, "EGRESOS TOTALES (COMPRAS)", data.totalPurchases(), styles, styles.kpiValueNegative());
    }

    private void createKpiCard(Sheet sheet, int row, int col, String label, BigDecimal value, ReportStyles styles, CellStyle valueStyle) {
        createCell(sheet, row, col, label, styles.kpiLabel());
        sheet.addMergedRegion(new CellRangeAddress(row, row, col, col + 1));

        Row valueRow = sheet.getRow(row + 1) == null ? sheet.createRow(row + 1) : sheet.getRow(row + 1);
        valueRow.setHeightInPoints(30);
        createCell(sheet, row + 1, col, value.doubleValue(), valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(row + 1, row + 2, col, col + 1));

        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(new CellRangeAddress(row, row + 2, col, col + 1), BorderStyle.THIN, COLOR_PRIMARY_DARK.getIndex(), BorderExtent.ALL);
        pt.applyBorders(sheet);
    }

    private void createSalesDetailSheet(Sheet sheet, List<SalesReportRow> rows, ReportStyles styles) {
        String[] headers = {"Fecha", "ID Orden", "Cliente", "Producto", "Cant.", "P. Venta", "Total Venta", "Costo", "Ganancia"};
        sheet.createFreezePane(0, 1);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) createCell(headerRow, i, headers[i], styles.header());

        // <-- CAMBIO: Establecer anchos de columna fijos
        sheet.setColumnWidth(0, 3500);  // Fecha
        sheet.setColumnWidth(1, 2500);  // ID Orden
        sheet.setColumnWidth(2, 8000);  // Cliente
        sheet.setColumnWidth(3, 8000);  // Producto
        sheet.setColumnWidth(4, 2500);  // Cant.
        sheet.setColumnWidth(5, 4000);  // P. Venta
        sheet.setColumnWidth(6, 4000);  // Total Venta
        sheet.setColumnWidth(7, 4000);  // Costo
        sheet.setColumnWidth(8, 4000);  // Ganancia

        int rowIdx = 1;
        for (SalesReportRow rowData : rows) {
            Row row = sheet.createRow(rowIdx++);
            int col = 0;
            // <-- CAMBIO: Aplicar estilos centrados
            createCell(row, col++, rowData.date(), styles.dateCentered());
            createCell(row, col++, rowData.orderId(), styles.centered());
            createCell(row, col++, rowData.customer(), styles.normal()); // Nombres a la izquierda se ve mejor
            createCell(row, col++, rowData.product(), styles.normal());  // Nombres a la izquierda se ve mejor
            createCell(row, col++, rowData.quantity().doubleValue(), styles.centered());
            createCell(row, col++, rowData.unitPrice().doubleValue(), styles.currencyCentered());
            createCell(row, col++, rowData.totalSale().doubleValue(), styles.currencyCentered());
            createCell(row, col++, rowData.totalCost().doubleValue(), styles.currencyCentered());
            createCell(row, col++, rowData.profit().doubleValue(), rowData.profit().compareTo(BigDecimal.ZERO) >= 0 ? styles.currencyPositiveCentered() : styles.currencyNegativeCentered());
        }

        if (!rows.isEmpty()) {
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.setHeightInPoints(20);
            createCell(totalRow, 5, "TOTALES:", styles.header());
            addFormula(totalRow, 6, "SUM", rowIdx, styles.currencyBold());
            addFormula(totalRow, 7, "SUM", rowIdx, styles.currencyBold());
            addFormula(totalRow, 8, "SUM", rowIdx, styles.currencyBold());
        }
    }

    private void createPurchasesDetailSheet(Sheet sheet, List<PurchaseReportRow> rows, ReportStyles styles) {
        String[] headers = {"Fecha", "ID Orden", "Proveedor", "Producto", "Cantidad", "Costo Unitario", "Total Costo"};
        sheet.createFreezePane(0, 1);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) createCell(headerRow, i, headers[i], styles.header());

        // <-- CAMBIO: Establecer anchos de columna fijos
        sheet.setColumnWidth(0, 3500); // Fecha
        sheet.setColumnWidth(1, 2500); // ID Orden
        sheet.setColumnWidth(2, 8000); // Proveedor
        sheet.setColumnWidth(3, 8000); // Producto
        sheet.setColumnWidth(4, 2500); // Cantidad
        sheet.setColumnWidth(5, 4000); // Costo Unitario
        sheet.setColumnWidth(6, 4000); // Total Costo

        int rowIdx = 1;
        for (PurchaseReportRow rowData : rows) {
            Row row = sheet.createRow(rowIdx++);
            int col = 0;
            // <-- CAMBIO: Aplicar estilos centrados
            createCell(row, col++, rowData.date(), styles.dateCentered());
            createCell(row, col++, rowData.orderId(), styles.centered());
            createCell(row, col++, rowData.supplier(), styles.normal()); // Nombres a la izquierda se ve mejor
            createCell(row, col++, rowData.product(), styles.normal());  // Nombres a la izquierda se ve mejor
            createCell(row, col++, rowData.quantity().doubleValue(), styles.centered());
            createCell(row, col++, rowData.unitCost().doubleValue(), styles.currencyCentered());
            createCell(row, col++, rowData.totalCost().doubleValue(), styles.currencyCentered());
        }

        if (!rows.isEmpty()) {
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.setHeightInPoints(20);
            createCell(totalRow, 5, "TOTAL:", styles.header());
            addFormula(totalRow, 6, "SUM", rowIdx, styles.currencyBold());
        }
    }

    private Cell createCell(Sheet sheet, int r, int c, Object value, CellStyle style) {
        Row row = sheet.getRow(r);
        if (row == null) row = sheet.createRow(r);
        return createCell(row, c, value, style);
    }

    private Cell createCell(Row row, int c, Object value, CellStyle style) {
        Cell cell = row.createCell(c);
        if (value instanceof String) cell.setCellValue((String) value);
        else if (value instanceof Double) cell.setCellValue((Double) value);
        else if (value instanceof Integer) cell.setCellValue((Integer) value);
        else if (value instanceof Long) cell.setCellValue((Long) value);
        else if (value instanceof LocalDate) cell.setCellValue((LocalDate) value);
        if (style != null) cell.setCellStyle(style);
        return cell;
    }

    private void addFormula(Row row, int col, String formulaType, int lastDataRow, CellStyle style) {
        char colLetter = (char) ('A' + col);
        String formula = String.format("%s(%c2:%c%d)", formulaType, colLetter, colLetter, lastDataRow);
        Cell cell = row.createCell(col);
        cell.setCellFormula(formula);
        cell.setCellStyle(style);
    }

    // <-- CAMBIO: Añadidos nuevos estilos centrados
    private record ReportStyles(
            CellStyle title, CellStyle subtle, CellStyle header, CellStyle normal, CellStyle centered,
            CellStyle kpiLabel, CellStyle kpiValuePositive, CellStyle kpiValueNegative,
            CellStyle currency, CellStyle currencyBold, CellStyle currencyPositive, CellStyle currencyNegative, CellStyle date,
            CellStyle currencyCentered, CellStyle currencyPositiveCentered, CellStyle currencyNegativeCentered, CellStyle dateCentered
    ) {
        public ReportStyles(Workbook wb) {
            this(
                    createTitleStyle(wb), createSubtleStyle(wb), createHeaderStyle(wb), createNormalStyle(wb), createCenteredStyle(wb),
                    createKpiLabelStyle(wb), createKpiValueStyle(wb, COLOR_ACCENT_GREEN), createKpiValueStyle(wb, COLOR_ACCENT_RED),
                    createCurrencyStyle(wb, false, COLOR_FONT_DARK, false), createCurrencyStyle(wb, true, COLOR_FONT_DARK, false),
                    createCurrencyStyle(wb, false, COLOR_ACCENT_GREEN, false), createCurrencyStyle(wb, false, COLOR_ACCENT_RED, false),
                    createDateStyle(wb, false),
                    createCurrencyStyle(wb, false, COLOR_FONT_DARK, true), createCurrencyStyle(wb, false, COLOR_ACCENT_GREEN, true),
                    createCurrencyStyle(wb, false, COLOR_ACCENT_RED, true), createDateStyle(wb, true)
            );
        }
        private static CellStyle createBaseStyle(Workbook wb) { CellStyle style = wb.createCellStyle(); style.setFont(createFont(wb, 11, false, COLOR_FONT_DARK)); return style; }
        private static Font createFont(Workbook wb, int size, boolean isBold, IndexedColors color) { Font font = wb.createFont(); font.setFontName(FONT_NAME); font.setFontHeightInPoints((short) size); font.setBold(isBold); font.setColor(color.getIndex()); return font; }
        private static CellStyle createTitleStyle(Workbook wb) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 18, true, COLOR_PRIMARY_DARK)); return style; }
        private static CellStyle createSubtleStyle(Workbook wb) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 9, false, COLOR_FONT_SUBTLE)); style.setAlignment(HorizontalAlignment.RIGHT); return style; }
        private static CellStyle createHeaderStyle(Workbook wb) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 11, true, COLOR_FONT_LIGHT)); style.setFillForegroundColor(COLOR_PRIMARY_DARK.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND); style.setAlignment(HorizontalAlignment.CENTER); return style; }
        private static CellStyle createNormalStyle(Workbook wb) { return createBaseStyle(wb); }
        private static CellStyle createCenteredStyle(Workbook wb) { CellStyle style = createBaseStyle(wb); style.setAlignment(HorizontalAlignment.CENTER); return style; }
        private static CellStyle createKpiLabelStyle(Workbook wb) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 9, false, COLOR_FONT_SUBTLE)); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(VerticalAlignment.BOTTOM); return style; }
        private static CellStyle createKpiValueStyle(Workbook wb, IndexedColors color) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 22, true, color)); style.setDataFormat(wb.createDataFormat().getFormat("$ #,##0.00")); style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(VerticalAlignment.CENTER); return style; }
        private static CellStyle createCurrencyStyle(Workbook wb, boolean isBold, IndexedColors color, boolean isCentered) { CellStyle style = createBaseStyle(wb); style.setFont(createFont(wb, 11, isBold, color)); style.setDataFormat(wb.createDataFormat().getFormat("$ #,##0.00")); if (isCentered) style.setAlignment(HorizontalAlignment.CENTER); return style; }
        private static CellStyle createDateStyle(Workbook wb, boolean isCentered) { CellStyle style = createBaseStyle(wb); style.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy")); if (isCentered) style.setAlignment(HorizontalAlignment.CENTER); return style; }
    }
}