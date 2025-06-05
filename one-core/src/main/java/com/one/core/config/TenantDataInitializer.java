package com.one.core.config; // O el paquete que hayas elegido

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class TenantDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TenantDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public TenantDataInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.info("Attempting to initialize data for 'tenant_empresa_test' if not already present...");

        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS tenant_empresa_test;");
            logger.info("Schema 'tenant_empresa_test' ensured to exist or already exists.");

            Resource resource = resourceLoader.getResource("classpath:db/seed/V1_initial_tenant_schema.sql"); // Ajusta el path si lo cambiaste
            if (!resource.exists()) {
                logger.warn("Seed script 'classpath:db/seed/V1_initial_tenant_schema.sql' not found. Skipping data initialization for tenant_empresa_test.");
                return;
            }

            String sqlScript = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            try (Connection connection = jdbcTemplate.getDataSource().getConnection();
                 Statement statement = connection.createStatement()) {

                String[] individualStatements = sqlScript.replaceAll("(?m)^--.*?$", "").split(";");

                int statementsExecuted = 0;
                for (String singleSql : individualStatements) {
                    String trimmedSql = singleSql.trim();
                    if (!trimmedSql.isEmpty()) {
                        try {
                            statement.execute(trimmedSql);
                            statementsExecuted++;
                        } catch (Exception e) {
                            logger.error("Error executing statement for tenant_empresa_test: [{}] Error: {}", trimmedSql, e.getMessage());
                        }
                    }
                }
                if (statementsExecuted > 0) {
                    logger.info("Successfully executed {} statements from script for tenant_empresa_test.", statementsExecuted);
                } else {
                    logger.info("No executable statements found or executed from script for tenant_empresa_test.");
                }
            }

        } catch (Exception e) {
            logger.error("Failed to initialize data for tenant_empresa_test: {}", e.getMessage(), e);

        }
    }
}