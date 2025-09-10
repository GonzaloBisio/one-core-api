package com.one.core.domain.model.tenant.gym;

import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_membresia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificacionMembresia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;
    
    @Column(nullable = false, length = 50)
    private String tipoNotificacion; // "VENCIMIENTO_5_DIAS", "VENCIMIENTO_10_DIAS", "VENCIDA"
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;
    
    @Column(name = "dias_restantes")
    private Integer diasRestantes;
    
    @Column(name = "is_enviada", nullable = false)
    private boolean isEnviada = false;
    
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
    
    @Column(name = "canal_envio", length = 20)
    private String canalEnvio; // "EMAIL", "SMS", "PUSH", "IN_APP"
    
    @Column(name = "is_leida", nullable = false)
    private boolean isLeida = false;
    
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
    
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
