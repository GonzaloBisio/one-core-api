package com.one.core.domain.service.admin;

import com.one.core.application.dto.admin.TenantCreationRequestDTO;
import com.one.core.application.dto.admin.TenantUserCreateRequestDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Transactional
    public Tenant createTenantMetadata(TenantCreationRequestDTO requestDTO) {
        // --- Lógica existente sin cambios ---
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
        // --- VALIDACIÓN AÑADIDA ---
        if (systemUserRepository.existsByEmail(requestDTO.getAdminEmail())) {
            throw new DuplicateFieldException("Admin Email", requestDTO.getAdminEmail());
        }

        Tenant newTenant = new Tenant();
        newTenant.setCompanyName(requestDTO.getCompanyName());
        newTenant.setSchemaName(schemaName);
        newTenant.setIndustryType(requestDTO.getIndustryType());
        Tenant savedTenant = tenantRepository.save(newTenant);

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

    // --- MÉTODO MODIFICADO ---
    public void provisionTenantInfrastructure(Tenant tenant) {
        String schemaName = tenant.getSchemaName();
        if (schemaName == null || schemaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Schema name for provisioning cannot be null or empty.");
        }

        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");

        // Lógica para decidir qué scripts ejecutar
        List<String> migrationLocations = new ArrayList<>();
        migrationLocations.add("classpath:db/migration/tenant/common"); // Siempre las tablas comunes

        switch (tenant.getIndustryType()) {
            case FOOD_AND_BEVERAGE:
                migrationLocations.add("classpath:db/migration/tenant/food_and_beverage");
                break;
            case DISTRIBUTION:
                migrationLocations.add("classpath:db/migration/tenant/distribution");
                break;
            case GYM:
                migrationLocations.add("classpath:db/migration/tenant/gym");
                break;
        }

        // Ejecutar Flyway con las carpetas seleccionadas
        flywayTenantMigrationService.migrateTenantSchema(schemaName, migrationLocations.toArray(new String[0]));
    }

    @Transactional
    public SystemUser createTenantUser(TenantUserCreateRequestDTO dto) {
        Tenant tenant = tenantRepository.findBySchemaName(dto.getSchemaName())
                .orElseThrow(() -> new NoSuchElementException("Tenant not found: " + dto.getSchemaName()));

        if (systemUserRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateFieldException("Username", dto.getUsername());
        }
        if (dto.getEmail() != null && systemUserRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateFieldException("Email", dto.getEmail());
        }

        SystemUser user = new SystemUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        user.setSystemRole(SystemRole.TENANT_USER);
        user.setTenant(tenant);

        return systemUserRepository.save(user);
    }
}