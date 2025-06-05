// src/main/java/com/one/core/domain/service/admin/TenantAdminService.java
package com.one.core.domain.service.admin;

import com.one.core.application.dto.admin.TenantCreationRequestDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.admin.Tenant;
import com.one.core.domain.model.enums.SystemRole;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.admin.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class TenantAdminService {

    private final TenantRepository tenantRepository;
    private final SystemUserRepository systemUserRepository;
    private final FlywayTenantMigrationService flywayTenantMigrationService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TenantAdminService(TenantRepository tenantRepository,
                              SystemUserRepository systemUserRepository,
                              FlywayTenantMigrationService flywayTenantMigrationService,
                              PasswordEncoder passwordEncoder,
                              JdbcTemplate jdbcTemplate) {
        this.tenantRepository = tenantRepository;
        this.systemUserRepository = systemUserRepository;
        this.flywayTenantMigrationService = flywayTenantMigrationService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Paso 1: Crea los metadatos del tenant y su usuario administrador.
     * Esta operación es transaccional.
     */
    @Transactional
    public Tenant createTenantMetadata(TenantCreationRequestDTO requestDTO) {
        // --- Validaciones Previas ---
        String schemaName = "tenant_" + requestDTO.getSchemaIdentifier();
        if (tenantRepository.existsBySchemaName(schemaName)) {
            throw new DuplicateFieldException("Schema Name", schemaName);
        }
        if (systemUserRepository.existsByUsername(requestDTO.getAdminUsername())) {
            throw new DuplicateFieldException("Admin Username", requestDTO.getAdminUsername());
        }
        if (tenantRepository.existsByCompanyName(requestDTO.getCompanyName())) {
            throw new DuplicateFieldException("Company Name", requestDTO.getCompanyName());
        }

        // --- Crear el Registro del Tenant ---
        Tenant newTenant = new Tenant();
        newTenant.setCompanyName(requestDTO.getCompanyName());
        newTenant.setSchemaName(schemaName);
        Tenant savedTenant = tenantRepository.save(newTenant);

        // --- Crear el SystemUser Administrador para el Tenant ---
        SystemUser tenantAdmin = new SystemUser();
        tenantAdmin.setUsername(requestDTO.getAdminUsername());
        tenantAdmin.setPassword(passwordEncoder.encode(requestDTO.getAdminPassword()));
        tenantAdmin.setEmail(requestDTO.getAdminEmail());
        tenantAdmin.setName(requestDTO.getAdminName());
        tenantAdmin.setLastName(requestDTO.getAdminLastName());
        tenantAdmin.setSystemRole(SystemRole.TENANT_ADMIN);
        tenantAdmin.setActivo(true);
        tenantAdmin.setTenant(savedTenant);
        systemUserRepository.save(tenantAdmin);

        return savedTenant;
    }

    /**
     * Paso 2: Provisiona la infraestructura del tenant (schema y tablas).
     * Esta operación se ejecuta fuera de la transacción principal para evitar bloqueos.
     */
    // La ausencia de @Transactional significa que cada operación (CREATE SCHEMA, flyway.migrate)
    // se ejecutará en su propia transacción corta (o será auto-committed), lo cual es correcto para DDL.
    public void provisionTenantInfrastructure(String schemaName) {
        if (schemaName == null || schemaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Schema name for provisioning cannot be null or empty.");
        }
        // --- Crear el Schema en la Base de Datos ---
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");

        // --- Ejecutar Migraciones de Flyway para el Nuevo Schema ---
        flywayTenantMigrationService.migrateTenantSchema(schemaName);
    }
}