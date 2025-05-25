package com.one.core.config.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("currentTenantIdentifierResolver")
public class JwtTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Value("${spring.datasource.default-schema}") // ej. "public"
    private String defaultTenant;

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenant();
        return (tenantId != null) ? tenantId : defaultTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}