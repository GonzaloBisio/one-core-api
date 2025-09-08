package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.admin.SystemUser;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity @Table(name = "classes")
public class ClassTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "class_type_id", nullable = false)
    private ClassType classType;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "rrule", nullable = false, columnDefinition = "TEXT")
    private String rrule;

    @Column(name = "start_time_local", nullable = false)
    private LocalTime startTimeLocal;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    // si null usa capacity_default de la sala
    private Integer capacity;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isActive = true;

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
