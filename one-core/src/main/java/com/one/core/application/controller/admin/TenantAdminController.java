// src/main/java/com/one/core/application/controller/admin/TenantAdminController.java
package com.one.core.application.controller.admin;

import com.one.core.application.dto.admin.TenantCreationRequestDTO;
import com.one.core.domain.model.admin.Tenant;
import com.one.core.domain.service.admin.TenantAdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tenants")
public class TenantAdminController {

    private static final Logger logger = LoggerFactory.getLogger(TenantAdminController.class);
    private final TenantAdminService tenantAdminService;

    @Autowired
    public TenantAdminController(TenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createTenant(@Valid @RequestBody TenantCreationRequestDTO requestDTO) {

        Tenant newTenant = tenantAdminService.createTenantMetadata(requestDTO);

        try {
            // --- CAMBIO: Pasar el objeto 'newTenant' completo ---
            tenantAdminService.provisionTenantInfrastructure(newTenant);
        } catch (Exception e) {
            logger.error("Tenant metadata was created (ID: {}), but infrastructure provisioning failed. Manual intervention may be required.", newTenant.getId(), e);
            String errorMessage = "Tenant user and record were created, but failed to provision the database schema. Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }

        logger.info("Successfully created and provisioned new tenant: {}", newTenant.getCompanyName());
        return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
    }
}