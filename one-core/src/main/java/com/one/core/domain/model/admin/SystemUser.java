package com.one.core.domain.model.admin;

import com.one.core.domain.model.enums.SystemRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "system_users", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", length = 50, nullable = false)
    private SystemRole systemRole;

    @ManyToOne(fetch = FetchType.LAZY) // Carga LAZY es buena práctica
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
}