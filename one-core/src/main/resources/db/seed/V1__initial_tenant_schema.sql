-- =================================================================
-- SCRIPT DE MIGRACIÓN GENÉRICO PARA NUEVOS TENANTS (V1)
-- Ubicación: src/main/resources/db/migration/tenant/V1__initial_tenant_schema.sql
--
-- NOTA: No se especifica ningún nombre de schema (ej. tenant_empresa_test).
-- Flyway, al ser invocado programáticamente para un schema específico,
-- aplicará estos CREATE TABLE dentro de ese schema.
-- =================================================================

-- -----------------------------------------------------
-- Tabla: suppliers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
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
COMMENT ON TABLE suppliers IS 'Proveedores de productos o servicios para la empresa.';

-- -----------------------------------------------------
-- Tabla: product_categories
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS product_categories (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pc_parent_category FOREIGN KEY (parent_category_id)
    REFERENCES product_categories (id) ON DELETE SET NULL
    );
COMMENT ON TABLE product_categories IS 'Categorías para organizar los productos.';

-- -----------------------------------------------------
-- Tabla: products
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        sku VARCHAR(50) UNIQUE,
    name VARCHAR(150) NOT NULL,
    product_type VARCHAR(50) NOT NULL DEFAULT 'PHYSICAL_GOOD',
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
    created_by_user_id BIGINT, -- FK a public.system_users.id
    updated_by_user_id BIGINT, -- FK a public.system_users.id
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
    REFERENCES product_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_default_supplier FOREIGN KEY (default_supplier_id)
    REFERENCES suppliers (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_updated_by FOREIGN KEY (updated_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE products IS 'Catálogo de productos/servicios que ofrece la empresa.';

-- -----------------------------------------------------
-- Tabla: customers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
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
COMMENT ON TABLE customers IS 'Clientes de la empresa (del tenant).';

-- -----------------------------------------------------
-- Tabla: sales_orders
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_orders (
                                            id BIGSERIAL PRIMARY KEY,
                                            customer_id BIGINT,
                                            order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                            status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    subtotal_amount NUMERIC(14, 2) DEFAULT 0.00,
    tax_amount NUMERIC(14, 2) DEFAULT 0.00,
    discount_amount NUMERIC(14, 2) DEFAULT 0.00,
    total_amount NUMERIC(14, 2) DEFAULT 0.00,
    payment_method VARCHAR(50),
    shipping_address TEXT,
    notes TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id)
    REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_so_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE sales_orders IS 'Pedidos de venta o facturas emitidas a clientes.';

-- -----------------------------------------------------
-- Tabla: sales_order_items
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_order_items (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 sales_order_id BIGINT NOT NULL,
                                                 product_id BIGINT NOT NULL,
                                                 quantity NUMERIC(10, 3) NOT NULL,
    unit_price_at_sale NUMERIC(12, 2) NOT NULL,
    discount_per_item NUMERIC(12, 2) DEFAULT 0.00,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_soi_sales_order FOREIGN KEY (sales_order_id)
    REFERENCES sales_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id)
    REFERENCES products (id) ON DELETE RESTRICT
    );
COMMENT ON TABLE sales_order_items IS 'Ítems detallados en cada pedido de venta o factura.';

-- -----------------------------------------------------
-- Tabla: purchase_orders
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_orders (
                                               id BIGSERIAL PRIMARY KEY,
                                               supplier_id BIGINT NOT NULL,
                                               order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                               expected_delivery_date DATE,
                                               status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(14, 2) DEFAULT 0.00,
    notes TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id)
    REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE purchase_orders IS 'Órdenes de compra realizadas a los proveedores.';

-- -----------------------------------------------------
-- Tabla: purchase_order_items
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_order_items (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    purchase_order_id BIGINT NOT NULL,
                                                    product_id BIGINT NOT NULL,
                                                    quantity_ordered NUMERIC(10, 3) NOT NULL,
    quantity_received NUMERIC(10, 3) DEFAULT 0.000,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_poi_purchase_order FOREIGN KEY (purchase_order_id)
    REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id)
    REFERENCES products (id) ON DELETE RESTRICT
    );
COMMENT ON TABLE purchase_order_items IS 'Ítems detallados en cada orden de compra.';

-- -----------------------------------------------------
-- Tabla: stock_movements (se movió aquí para que sus FKs a products, etc., sean válidas)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_movements (
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
    CONSTRAINT fk_sm_product FOREIGN KEY (product_id)
    REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sm_user FOREIGN KEY (user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE stock_movements IS 'Historial de todos los movimientos de entrada y salida de stock.';
CREATE INDEX IF NOT EXISTS idx_sm_product_id ON stock_movements(product_id);
