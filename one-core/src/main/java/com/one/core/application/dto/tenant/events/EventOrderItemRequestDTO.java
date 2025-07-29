package com.one.core.application.dto.tenant.events;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EventOrderItemRequestDTO {
    @NotNull
    private Long productId;
    @NotNull @Positive
    private BigDecimal quantity;
    private BigDecimal unitPrice; // Opcional, si no se provee se usa el de la lista de precios
}