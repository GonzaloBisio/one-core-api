package com.one.core.domain.model.tenant.gym;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ejercicios_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EjercicioPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_entrenamiento_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ejercicios"})
    private PlanEntrenamiento planEntrenamiento;
    
    @Column(nullable = false, length = 150)
    private String nombreEjercicio;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "series")
    private Integer series;
    
    @Column(name = "repeticiones")
    private String repeticiones; // "8-12", "15", "hasta el fallo"
    
    @Column(name = "peso_sugerido")
    private String pesoSugerido; // "20-30kg", "peso corporal"
    
    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;
    
    @Column(name = "musculos_trabajados", length = 200)
    private String musculosTrabajados;
    
    @Column(name = "equipamiento", length = 200)
    private String equipamiento;
    
    @Column(name = "orden")
    private Integer orden; // Para ordenar ejercicios en el plan
    
    @Column(name = "dificultad", length = 20)
    private String dificultad; // "FÁCIL", "MEDIO", "DIFÍCIL"
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "video_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ejercicioPlan"})
    private VideoEntrenamiento video;
    
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
