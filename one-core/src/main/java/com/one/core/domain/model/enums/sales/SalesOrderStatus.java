package com.one.core.domain.model.enums.sales;

public enum SalesOrderStatus {
    PENDING_PAYMENT, // Esperando pago
    PREPARING_ORDER,      // Pago recibido, en preparación
    SHIPPED,         // Enviado
    DELIVERED,       // Entregado al cliente
    COMPLETED,       // Pedido finalizado y cerrado
    CANCELLED,       // Pedido cancelado
    REFUNDED         // Pedido devuelto y dinero reembolsado
}