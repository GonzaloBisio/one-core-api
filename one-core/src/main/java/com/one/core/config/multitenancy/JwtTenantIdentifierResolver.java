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

    private final String defaultTenant;

    public JwtTenantIdentifierResolver(@Value("${TENANT_SCHEMA}") String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantIdFromContext = TenantContext.getCurrentTenant();
        String resolvedTenant = (tenantIdFromContext != null && !tenantIdFromContext.trim().isEmpty()) ?
                tenantIdFromContext : this.defaultTenant;
        return resolvedTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}