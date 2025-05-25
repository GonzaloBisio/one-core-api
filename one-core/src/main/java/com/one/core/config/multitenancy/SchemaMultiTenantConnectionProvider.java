package com.one.core.config.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component("multiTenantConnectionProvider")
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);
    private final DataSource dataSource;

    @Value("${spring.datasource.default-schema}")
    private String defaultTenantSchema;

    @Autowired
    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
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
        try (Statement statement = connection.createStatement()) {
            // Para PostgreSQL, cambia el search_path al esquema del tenant.
            // Asegúrate de sanitizar tenantIdentifier si es necesario, aunque aquí debería ser seguro
            // ya que proviene de tu lógica interna (JWT/TenantContext).
            String sql = String.format("SET search_path TO %s", tenantIdentifier);
            statement.execute(sql);
            LOGGER.debug("Switched to schema: {}", tenantIdentifier);
        } catch (SQLException e) {
            LOGGER.error("Could not switch to schema [{}]: {}", tenantIdentifier, e.getMessage());
            throw new SQLException("Could not switch to schema " + tenantIdentifier, e);
        }
        return connection;
    }


    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Vuelve al esquema por defecto (o público) al liberar la conexión.
            // Esto es importante para que el pool de conexiones no quede "contaminado"
            // con el search_path de un tenant específico para la siguiente vez que se use `getAnyConnection`.
            String sql = String.format("SET search_path TO %s", defaultTenantSchema);
            statement.execute(sql);
            LOGGER.debug("Reset schema to: {}", defaultTenantSchema);
        } catch (SQLException e) {
            LOGGER.error("Could not reset schema for tenant [{}]: {}", tenantIdentifier, e.getMessage());
            // No relanzar la excepción aquí para permitir que la conexión se cierre.
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true; // Permite la liberación agresiva de conexiones
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}