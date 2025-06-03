package com.one.core.application.dto.tenant.sales;

import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderFilterDTO {

    private Long customerId;    // Para filtrar por el ID del cliente asociado a la orden

    private SalesOrderStatus status; // Para filtrar por el estado actual de la orden (ej. PENDING, SHIPPED)

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Asegura que Spring parsee bien la fecha desde el request param
    private LocalDate orderDateFrom; // Para filtrar órdenes creadas desde esta fecha

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDateTo;   // Para filtrar órdenes creadas hasta esta fecha

    private BigDecimal minTotalAmount; // Para filtrar órdenes con un monto total mínimo

    private BigDecimal maxTotalAmount; // Para filtrar órdenes con un monto total máximo

    private String createdByUsername; // Para filtrar por el username del usuario del tenant que creó la orden

    // Considera añadir otros filtros comunes si los necesitas, por ejemplo:
    // private String productName; // Si quieres filtrar órdenes que contengan un producto específico (requeriría un subquery o join más complejo en la Specification)
    // private String paymentMethod;
}