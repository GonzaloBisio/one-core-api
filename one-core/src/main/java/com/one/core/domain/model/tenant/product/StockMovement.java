package com.one.core.domain.model.tenant.product;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.movements.MovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Column(name = "quantity_changed", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityChanged; // Positivo para entradas, negativo para salidas

    @Column(name = "stock_after_movement", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockAfterMovement;

    @Column(name = "movement_date", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime movementDate;

    @Column(name = "reference_document_type", length = 50)
    private String referenceDocumentType; // Ej: PURCHASE_ORDER, SALES_ORDER

    @Column(name = "reference_document_id", length = 100)
    private String referenceDocumentId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Usuario del tenant que realizó/causó el movimiento
    private SystemUser user;

    @PrePersist
    protected void onCreate() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }
}
