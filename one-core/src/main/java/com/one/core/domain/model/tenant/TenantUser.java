package com.one.core.domain.model.tenant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tenant_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Opcional: si quieres un vínculo directo al SystemUser global
    // @Column(name = "system_user_id", unique = true)
    // private Long systemUserId;

    @Column(nullable = false, unique = true, length = 100)
    private String username; // Username dentro del tenant, puede ser el mismo que el SystemUser

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER) // EAGER para cargar roles con el usuario
    @JoinTable(
            name = "tenant_user_has_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<TenantRole> roles;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
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