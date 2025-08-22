package com.one.core.domain.model.tenant.table;

import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_checks")
@Data
@EqualsAndHashCode(exclude = {"diningTable", "customer", "items", "payments"})
@ToString(exclude = {"diningTable", "customer", "items", "payments"})
@EntityListeners(AuditingEntityListener.class)
public class TableCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dining_table_id", nullable = false)
    private DiningTable diningTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "tableCheck", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TableCheckItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "tableCheck", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addItem(TableCheckItem item) {
        items.add(item);
        item.setTableCheck(this);
    }

    public void removeItem(TableCheckItem item) {
        items.remove(item);
        item.setTableCheck(null);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setTableCheck(this);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setTableCheck(null);
    }
}
