package com.one.core.config.multitenancy;

public final class TenantContext {

    private static final ThreadLocal<TenantInfo> currentTenant = new ThreadLocal<>();

    public static TenantInfo getCurrentTenant() {
        return currentTenant.get();
    }

    // Ahora el getter para el schema es un poco más seguro
    public static String getCurrentTenantSchema() {
        TenantInfo info = currentTenant.get();
        return (info != null) ? info.schemaName() : null;
    }

    // Y un getter para el rubro
    public static String getCurrentTenantIndustryType() {
        TenantInfo info = currentTenant.get();
        return (info != null) ? info.industryType() : null;
    }

    public static void setCurrentTenant(TenantInfo tenantInfo) {
        currentTenant.set(tenantInfo);
    }

    public static void clear() {
        currentTenant.remove();
    }
}