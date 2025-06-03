package com.one.core.domain.model.tenant.purchases;

import com.one.core.domain.model.tenant.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Data
@EqualsAndHashCode(exclude = {"purchaseOrder", "product"})
@ToString(exclude = {"purchaseOrder", "product"})
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_ordered", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_received", precision = 10, scale = 3)
    @ColumnDefault("0.000")
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2) // Precio de compra unitario
    private BigDecimal unitPrice;

    @Transient // Calculado, o puedes tenerlo como columna si la BD no es generada
    private BigDecimal subtotal;

    public BigDecimal getSubtotal() {
        if (quantityOrdered != null && unitPrice != null) {
            return quantityOrdered.multiply(unitPrice);
        }
        return BigDecimal.ZERO;
    }
    // Si el subtotal se persiste y no es GENERATED en BD:
    // @Column(name = "subtotal", precision = 14, scale = 2)
    // private BigDecimal subtotal;
    // @PrePersist @PreUpdate public void calculateSubtotal() { this.subtotal = getSubtotal(); }
}