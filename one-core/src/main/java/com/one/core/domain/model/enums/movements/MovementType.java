package com.one.core.domain.model.enums.movements;

public enum MovementType {
    INITIAL_STOCK,      // Carga inicial de stock
    PURCHASE_RECEIPT,   // Recepción de compra a proveedor
    SALE_CONFIRMED,     // Venta confirmada a cliente
    CUSTOMER_RETURN,    // Devolución de cliente
    SUPPLIER_RETURN,    // Devolución a proveedor
    ADJUSTMENT_IN,      // Ajuste manual de entrada
    ADJUSTMENT_OUT,     // Ajuste manual de salida (no por venta)
    WASTAGE             // Merma, pérdida, vencimiento
}
