package com.one.core.domain.model.tenant.gym;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos_entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VideoEntrenamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "url_video", length = 500)
    private String urlVideo; // URL externa (YouTube, Vimeo, etc.)
    
    @Column(name = "archivo_video", length = 500)
    private String archivoVideo; // Ruta del archivo subido
    
    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;
    
    @Column(name = "nivel_dificultad", length = 20)
    private String nivelDificultad; // "PRINCIPIANTE", "INTERMEDIO", "AVANZADO"
    
    @Column(name = "musculos_trabajados", length = 200)
    private String musculosTrabajados; // "PECHO, TRICEPS, HOMBROS"
    
    @Column(name = "equipamiento_necesario", length = 300)
    private String equipamientoNecesario;
    
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "orden")
    private Integer orden; // Para ordenar videos en un plan
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Instructor instructor;
    
    @OneToOne(mappedBy = "video", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "video"})
    private EjercicioPlan ejercicioPlan;
    
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
