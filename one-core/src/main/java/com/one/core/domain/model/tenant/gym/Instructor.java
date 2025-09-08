package com.one.core.domain.model.tenant.gym;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity @Table(name = "instructors")
public class Instructor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 150) private String email;
    @Column(length = 50)  private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
