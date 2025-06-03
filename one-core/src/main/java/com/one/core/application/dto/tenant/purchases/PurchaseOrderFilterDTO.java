package com.one.core.application.dto.tenant.purchases;

import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus; // Asegúrate de que este enum exista
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderFilterDTO {

    private Long supplierId;    // Filtrar por ID del proveedor

    private PurchaseOrderStatus status; // Filtrar por estado de la orden de compra

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDateFrom; // Rango de fechas de la orden: Desde

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDateTo;   // Rango de fechas de la orden: Hasta

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDateFrom; // Rango de fechas de entrega esperada: Desde

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDateTo;   // Rango de fechas de entrega esperada: Hasta

    private String createdByUsername; // Filtrar por el username del usuario del tenant que creó la orden

    // Podrías añadir más filtros como un rango para totalAmount si es necesario
}