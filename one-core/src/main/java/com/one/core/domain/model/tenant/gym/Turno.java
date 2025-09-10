package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.enums.gym.BookingStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Turno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(nullable = false)
    private LocalTime duracion; // Duración en minutos
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;
    
    @Column(length = 100)
    private String tipoTurno; // "Personal Training", "Consulta", "Evaluación", etc.
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = false;
    
    @Column(name = "recurring_pattern", length = 50)
    private String recurringPattern; // "WEEKLY", "BIWEEKLY", "MONTHLY"
    
    @Column(name = "recurring_end_date")
    private LocalDateTime recurringEndDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
