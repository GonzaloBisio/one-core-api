package com.one.core.application.dto.tenant.inventory;

import com.one.core.domain.model.enums.movements.MovementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate; // Usar LocalDate para filtrar por rangos de fechas sin hora

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementFilterDTO {
    private Long productId;
    private MovementType movementType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private Long tenantUserId;
}