package com.one.core.domain.model.enums.purchases;

public enum PurchaseOrderStatus {
    DRAFT,                // Borrador, la orden aún no se ha finalizado ni enviado
    ORDERED,              // Orden enviada al proveedor, esperando mercancía
    PARTIALLY_RECEIVED,   // Se ha recibido parte de la mercancía pedida
    FULLY_RECEIVED,       // Se ha recibido toda la mercancía pedida
    CANCELLED,            // Orden cancelada (antes o después de recibir algo, puede requerir lógica de reversión)
    CLOSED                // Orden completada y cerrada administrativamente
}