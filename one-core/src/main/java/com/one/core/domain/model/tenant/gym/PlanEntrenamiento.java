package com.one.core.domain.model.tenant.gym;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planes_entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PlanEntrenamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 150)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "nivel_dificultad", length = 20)
    private String nivelDificultad; // "PRINCIPIANTE", "INTERMEDIO", "AVANZADO"
    
    @Column(name = "duracion_semanas")
    private Integer duracionSemanas;
    
    @Column(name = "objetivo", length = 100)
    private String objetivo; // "PÉRDIDA DE PESO", "GANANCIA DE MÚSCULO", "RESISTENCIA", etc.
    
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Instructor instructor;
    
    @OneToMany(mappedBy = "planEntrenamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "planEntrenamiento"})
    private List<EjercicioPlan> ejercicios = new ArrayList<>();
    
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
