-- Creación del esquema 'public' si no existe (PostgreSQL lo tiene por defecto)
-- CREATE SCHEMA IF NOT EXISTS public

CREATE TABLE IF NOT EXISTS public.tenants (
                                id BIGSERIAL PRIMARY KEY,
                                company_name VARCHAR(100) NOT NULL UNIQUE,
                                schema_name VARCHAR(100) NOT NULL UNIQUE,
                                industry_type VARCHAR(50) NOT NULL DEFAULT 'RETAIL',
                                created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.system_users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(100) NOT NULL UNIQUE,
                                     password VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     name VARCHAR(100),
                                     last_name VARCHAR(100),
                                     activo BOOLEAN DEFAULT TRUE,
                                     tenant_id BIGINT NOT NULL,
                                     system_role VARCHAR(50) NOT NULL DEFAULT 'TENANT_USER',
                                     CONSTRAINT fk_tenant
                                         FOREIGN KEY(tenant_id)
                                             REFERENCES public.tenants(id)
                                             ON DELETE CASCADE
);