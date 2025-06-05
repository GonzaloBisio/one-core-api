package com.one.core.domain.service.admin;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class FlywayTenantMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(FlywayTenantMigrationService.class);

    private final DataSource dataSource;

    @Autowired
    public FlywayTenantMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Aplica las migraciones de tenant al schema especificado.
     * @param schemaName El nombre del schema del nuevo tenant (ej. "tenant_ferreteria_la_tuerca")
     */
    public void migrateTenantSchema(String schemaName) {
        logger.info("Starting Flyway migration for new tenant schema: {}", schemaName);
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource) // Usa el mismo datasource de la aplicación
                    .schemas(schemaName)    // ¡IMPORTANTE! Especifica el schema a migrar
                    .locations("classpath:db/migration/tenant") // Apunta a los scripts para tenants
                    .baselineOnMigrate(true) // Crea la tabla de historial de Flyway si no existe en este schema
                    .load();

            flyway.migrate();
            logger.info("Flyway migration completed successfully for schema: {}", schemaName);
        } catch (Exception e) {
            logger.error("Failed to apply Flyway migration for schema: {}", schemaName, e);
            // Lanza una excepción personalizada para que la transacción principal haga rollback
            throw new RuntimeException("Failed to migrate new tenant schema: " + schemaName, e);
        }
    }
}
