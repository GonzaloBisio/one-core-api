package com.one.core.domain.model.tenant.sales;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Data
@EqualsAndHashCode(exclude = {"items", "packagingItems", "customer", "createdByUser"}) // Evitar problemas con Lombok y relaciones
@ToString(exclude = {"items", "packagingItems", "customer", "createdByUser"})
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesOrderStatus status;

    @Column(name = "subtotal_amount", precision = 14, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 14, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 14, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 14, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private SystemUser createdByUser;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SalesOrderPackaging> packagingItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (status == null) status = SalesOrderStatus.PENDING_PAYMENT;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Helper methods para añadir/quitar items y recalcular totales
    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
        recalculateTotals();
    }

    public void removeItem(SalesOrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotalAmount = items.stream()
                .map(SalesOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Lógica de impuestos y descuentos simplificada. Podrías tener reglas más complejas.
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
        // Asume que taxAmount se calcula sobre (subtotalAmount - discountAmount) o se setea externamente
        if (this.taxAmount == null) this.taxAmount = BigDecimal.ZERO;

        this.totalAmount = this.subtotalAmount.subtract(this.discountAmount).add(this.taxAmount);
    }
}