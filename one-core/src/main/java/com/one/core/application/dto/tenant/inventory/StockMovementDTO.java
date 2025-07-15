package com.one.core.application.dto.tenant.inventory;

import com.one.core.domain.model.enums.movements.MovementType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDTO {
    private Long id;
    private Long productId;
    private String productName;
    private MovementType movementType;
    private BigDecimal quantityChanged;
    private BigDecimal stockAfterMovement;
    private OffsetDateTime movementDate;
    private String referenceDocumentType;
    private String referenceDocumentId;
    private Long userId;
    private String username;
    private String notes;
}