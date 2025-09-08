package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.enums.gym.MembershipStatus;
import com.one.core.domain.model.enums.sales.PaymentMethod;
import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.one.core.domain.model.admin.SystemUser;

@Data
@Entity @Table(name = "memberships")
public class Membership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;
    private LocalDate nextBillingDate;

    @Column(nullable = false)
    private boolean autopay = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_payment_method", length = 50)
    private PaymentMethod preferredPaymentMethod;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_user_id")
    private SystemUser createdByUser;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "updated_by_user_id")
    private SystemUser updatedByUser;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
