package com.one.core.application.dto.tenant.inventory;

import com.one.core.domain.model.enums.movements.MovementType;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockAdjustmentRequestDTO {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity to adjust cannot be null")
    // No puede ser cero, debe ser positivo o negativo
    private BigDecimal quantityAdjusted; // Positivo para sumar, negativo para restar

    @NotNull(message = "Movement type for adjustment cannot be null")
    private MovementType adjustmentType; // Debería ser ADJUSTMENT_IN o ADJUSTMENT_OUT

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason; // Motivo del ajuste

    // El ID del usuario se obtendrá del contexto de seguridad (usuario autenticado) en el servicio
}