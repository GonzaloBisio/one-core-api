-- =================================================================
-- SCRIPT PARA CREAR TABLAS EN EL SCHEMA tenant_empresa_test
-- Ejecutar con el schema 'tenant_empresa_test' seleccionado o prefijando.
-- =================================================================

-- -----------------------------------------------------
-- Tabla: tenant_users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.tenant_users (
                                                                id BIGSERIAL PRIMARY KEY,
                                                                system_user_id BIGINT NULL UNIQUE,
                                                                username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    -- La FK a public.system_users debe referenciar correctamente el schema 'public'
    CONSTRAINT fk_system_user FOREIGN KEY (system_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

COMMENT ON TABLE tenant_empresa_test.tenant_users IS 'Usuarios (empleados) específicos de este tenant/empresa.';

-- -----------------------------------------------------
-- Tabla: tenant_roles
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.tenant_roles (
                                                                id BIGSERIAL PRIMARY KEY,
                                                                role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE tenant_empresa_test.tenant_roles IS 'Roles definidos específicamente para los usuarios dentro de este tenant.';

-- -----------------------------------------------------
-- Tabla: tenant_user_has_roles
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.tenant_user_has_roles (
                                                                         user_id BIGINT NOT NULL,
                                                                         role_id BIGINT NOT NULL,
                                                                         PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tenant_empresa_test.tenant_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES tenant_empresa_test.tenant_roles (id) ON DELETE CASCADE
    );
COMMENT ON TABLE tenant_empresa_test.tenant_user_has_roles IS 'Tabla de unión para asignar roles de tenant a usuarios de tenant.';


-- -----------------------------------------------------
-- Tabla: suppliers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.suppliers (
                                                             id BIGSERIAL PRIMARY KEY,
                                                             name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(50),
    address TEXT,
    tax_id VARCHAR(50) UNIQUE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE tenant_empresa_test.suppliers IS 'Proveedores de productos o servicios para la empresa.';

-- -----------------------------------------------------
-- Tabla: product_categories
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.product_categories (
                                                                      id BIGSERIAL PRIMARY KEY,
                                                                      name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_category_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_name UNIQUE (name),
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id) REFERENCES tenant_empresa_test.product_categories (id) ON DELETE SET NULL
    );

COMMENT ON TABLE tenant_empresa_test.product_categories IS 'Categorías para organizar los productos.';

-- -----------------------------------------------------
-- Tabla: products
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.products (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            sku VARCHAR(50) UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_id BIGINT,
    default_supplier_id BIGINT,
    purchase_price NUMERIC(12, 2) DEFAULT 0.00,
    sale_price NUMERIC(12, 2) DEFAULT 0.00,
    unit_of_measure VARCHAR(20) DEFAULT 'UNIT',
    current_stock NUMERIC(12, 3) DEFAULT 0.000,
    minimum_stock_level NUMERIC(12, 3) DEFAULT 0.000,
    is_active BOOLEAN DEFAULT TRUE,
    barcode VARCHAR(100),
    image_url VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    updated_by_user_id BIGINT,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES tenant_empresa_test.product_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_default_supplier FOREIGN KEY (default_supplier_id) REFERENCES tenant_empresa_test.suppliers (id) ON DELETE SET NULL,
    CONSTRAINT fk_created_by FOREIGN KEY (created_by_user_id) REFERENCES tenant_empresa_test.tenant_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES tenant_empresa_test.tenant_users (id) ON DELETE SET NULL
    );

COMMENT ON TABLE tenant_empresa_test.products IS 'Catálogo de productos/servicios que ofrece la empresa.';

-- -----------------------------------------------------
-- Tabla: stock_movements
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.stock_movements (
                                                                   id BIGSERIAL PRIMARY KEY,
                                                                   product_id BIGINT NOT NULL,
                                                                   movement_type VARCHAR(50) NOT NULL,
    quantity_changed NUMERIC(12, 3) NOT NULL,
    stock_after_movement NUMERIC(12, 3) NOT NULL,
    movement_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    reference_document_type VARCHAR(50),
    reference_document_id VARCHAR(100),
    notes TEXT,
    user_id BIGINT,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES tenant_empresa_test.products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_stock_movement FOREIGN KEY (user_id) REFERENCES tenant_empresa_test.tenant_users (id) ON DELETE SET NULL -- Nombre de FK cambiado para evitar colisión con la FK de tenant_user_has_roles si se juntan los scripts
    );

COMMENT ON TABLE tenant_empresa_test.stock_movements IS 'Historial de todos los movimientos de entrada y salida de stock.';

CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id ON tenant_empresa_test.stock_movements(product_id);

-- -----------------------------------------------------
-- Tabla: customers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.customers (
                                                             id BIGSERIAL PRIMARY KEY,
                                                             name VARCHAR(150) NOT NULL,
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL',
    tax_id VARCHAR(50) UNIQUE,
    email VARCHAR(100),
    phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );
COMMENT ON TABLE tenant_empresa_test.customers IS 'Clientes de la empresa (del tenant).';


