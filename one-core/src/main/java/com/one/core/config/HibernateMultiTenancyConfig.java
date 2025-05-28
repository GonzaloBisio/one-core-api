 package com.one.core.config;

 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
 import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

 import java.util.Map;

 @Configuration
 public class HibernateMultiTenancyConfig {

     @Bean
     public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
             @Qualifier("currentTenantIdentifierResolver") CurrentTenantIdentifierResolver<String> tenantResolver,
             @Qualifier("multiTenantConnectionProvider") MultiTenantConnectionProvider<String> mtConnectionProvider) {
         return hibernateProperties -> {
             hibernateProperties.put(org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, mtConnectionProvider);
             hibernateProperties.put(org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
             // MULTI_TENANCY ya está seteado a SCHEMA en application.yml, así que no es necesario aquí.
             // hibernateProperties.put(org.hibernate.cfg.AvailableSettings.MULTI_TENANCY, "SCHEMA");
         };
     }
 }