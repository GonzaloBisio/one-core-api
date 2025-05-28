// src/main/java/com/one/core/config/multitenancy/SchemaMultiTenantConnectionProvider.java
package com.one.core.config.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Necesario para el constructor
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource; // O jakarta.sql.DataSource
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component("multiTenantConnectionProvider")
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final Logger logger = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);
    private final DataSource dataSource;
    private final String defaultTenantSchema; // El campo ahora es final

    // Inyectar DataSource y defaultTenantSchema a través del constructor
    @Autowired
    public SchemaMultiTenantConnectionProvider(DataSource dataSource,
                                               @Value("${TENANT_SCHEMA}") String defaultTenantSchema) { // O @Value("${TENANT_SCHEMA:public}")
        this.dataSource = dataSource;
        this.defaultTenantSchema = defaultTenantSchema;
        logger.error("!!!!!!!! BEAN CREADO Y CONFIGURADO: SchemaMultiTenantConnectionProvider. Default Schema: '{}'", this.defaultTenantSchema);
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        logger.info("!!!!!!!! SCHEMA_MCP: Received tenantIdentifier: '{}' to set search_path.", tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            String sql = String.format("SET search_path TO \"%s\"", tenantIdentifier.replace("\"", "\"\""));
            logger.info("!!!!!!!! SCHEMA_MCP: Executing SQL: [{}]", sql);
            statement.execute(sql);
            logger.info("!!!!!!!! SCHEMA_MCP: Successfully switched search_path to: '{}'", tenantIdentifier);
        } catch (SQLException e) {
            logger.error("!!!!!!!! SCHEMA_MCP: ERROR switching schema to [{}]: {}", tenantIdentifier, e.getMessage(), e);
            throw new SQLException("Could not switch to schema " + tenantIdentifier, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = String.format("SET search_path TO \"%s\"", this.defaultTenantSchema.replace("\"", "\"\""));
            logger.debug("SchemaMultiTenantConnectionProvider - Resetting search_path to: {}", this.defaultTenantSchema);
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("SchemaMultiTenantConnectionProvider - Could not reset schema to default [{}]: {}", this.defaultTenantSchema, e.getMessage(), e);
        } finally {
            connection.close();
        }
    }

    // ... (supportsAggressiveRelease, isUnwrappableAs, unwrap sin cambios) ...
    @Override
    public boolean supportsAggressiveRelease() { return true; }
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) { return false; }
    @Override
    public <T> T unwrap(Class<T> unwrapType) { return null; }
}