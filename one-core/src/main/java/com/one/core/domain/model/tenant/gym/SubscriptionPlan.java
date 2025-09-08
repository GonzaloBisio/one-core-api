package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.enums.gym.GymAccessMode;
import com.one.core.domain.model.tenant.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;

import org.hibernate.type.SqlTypes;

@Data
@Entity @Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_mode", nullable = false, length = 30)
    private GymAccessMode accessMode = GymAccessMode.UNLIMITED;

    // Null si UNLIMITED
    private Integer visitsAllowed;

    // 1..7 si N_PER_WEEK
    private Short resetDayOfWeek;

    @Column(nullable = false)
    private Integer billingPeriodMonths = 1;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Postgres text[]
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_class_tags", columnDefinition = "text[]")
    private String[] allowedClassTags;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
