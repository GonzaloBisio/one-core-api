package com.one.core.application.dto.tenant.inventory;

import com.one.core.domain.model.enums.movements.MovementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate; // Usar LocalDate para filtrar por rangos de fechas sin hora
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementFilterDTO {
    private Long productId;
    // un solo tipo (opcional)
    private MovementType movementType;

    // o varios tipos (opcional) -> /inventory/movements?movementTypes=SALE_CONFIRMED,PACKAGING_CONSUMPTION
    private List<MovementType> movementTypes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private Long tenantUserId;
}