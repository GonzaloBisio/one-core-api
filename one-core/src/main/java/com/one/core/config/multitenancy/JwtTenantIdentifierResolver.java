// src/main/java/com/one/core/config/multitenancy/JwtTenantIdentifierResolver.java
package com.one.core.config.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("currentTenantIdentifierResolver")
public class JwtTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
    private static final Logger logger = LoggerFactory.getLogger(JwtTenantIdentifierResolver.class);

    private final String defaultTenant; // El campo ahora es final y se inicializa en el constructor

    // Inyectar a través del constructor
    public JwtTenantIdentifierResolver(@Value("${TENANT_SCHEMA}") String defaultTenant) { // O @Value("${TENANT_SCHEMA:public}") si quieres un default en código
        this.defaultTenant = defaultTenant;
        logger.error("!!!!!!!! BEAN CREADO Y CONFIGURADO: JwtTenantIdentifierResolver. Default Tenant: '{}'", this.defaultTenant);
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantIdFromContext = TenantContext.getCurrentTenant();
        logger.info("!!!!!!!! JWT_TENANT_RESOLVER: Tenant from Context: '{}'", tenantIdFromContext);
        String resolvedTenant = (tenantIdFromContext != null && !tenantIdFromContext.trim().isEmpty()) ?
                tenantIdFromContext : this.defaultTenant; // Usa el campo inicializado en el constructor
        logger.info("!!!!!!!! JWT_TENANT_RESOLVER: Resolved to: '{}'. (Default was: '{}')", resolvedTenant, this.defaultTenant);
        return resolvedTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}