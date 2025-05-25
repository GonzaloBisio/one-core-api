package com.one.core.domain.model.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // companyName se mapeará a company_name por la estrategia de nombrado implícita de Hibernate
    @Column(name = "company_name", nullable = false, unique = true, length = 100)
    private String companyName;

    // schemaName se mapeará a schema_name
    @Column(name = "schema_name", nullable = false, unique = true, length = 100)
    private String schemaName;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}