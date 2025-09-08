package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.gym.UsageReason;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Entity @Table(name = "membership_usage_events")
public class MembershipUsageEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id")
    private ClassSession session;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate = LocalDate.now();

    @Column(nullable = false)
    private Integer units = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private UsageReason reason;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_user_id")
    private SystemUser createdByUser;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }


}
