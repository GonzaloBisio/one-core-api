
package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.enums.gym.SessionStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity @Table(name = "class_sessions",
        uniqueConstraints = @UniqueConstraint(name="uq_session_unique", columnNames = {"class_id","start_at"}))
public class ClassSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_id", nullable = false)
    private ClassTemplate classTemplate;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "booked_count", nullable = false)
    private Integer bookedCount = 0;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
