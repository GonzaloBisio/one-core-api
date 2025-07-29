-- =================================================================
-- SCRIPT DE MIGRACIÓN GENÉRICO PARA NUEVOS TENANTS (V1)
-- Ubicación: src/main/resources/db/migration/tenant/V1__initial_tenant_schema.sql
-- =================================================================

CREATE TABLE IF NOT EXISTS suppliers (
                                         id BIGSERIAL PRIMARY KEY, name VARCHAR(150) NOT NULL, contact_person VARCHAR(100),
    email VARCHAR(100), phone VARCHAR(50), address TEXT, tax_id VARCHAR(50) UNIQUE, notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS product_categories (
                                                  id BIGSERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE, description TEXT, parent_category_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pc_parent_category FOREIGN KEY (parent_category_id) REFERENCES product_categories (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY, sku VARCHAR(50) UNIQUE, name VARCHAR(150) NOT NULL,
    product_type VARCHAR(50) NOT NULL DEFAULT 'PHYSICAL_GOOD',
    description TEXT,
    category_id BIGINT, default_supplier_id BIGINT, purchase_price NUMERIC(12, 2) DEFAULT 0.00,
    sale_price NUMERIC(12, 2) DEFAULT 0.00, unit_of_measure VARCHAR(20) DEFAULT 'UNIT',
    current_stock NUMERIC(12, 3) DEFAULT 0.000, minimum_stock_level NUMERIC(12, 3) DEFAULT 0.000,
    frozenStock NUMERIC(12, 3) DEFAULT 0.000,
    is_active BOOLEAN DEFAULT TRUE, barcode VARCHAR(100), image_url VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT, updated_by_user_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_default_supplier FOREIGN KEY (default_supplier_id) REFERENCES suppliers (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS customers (
                                         id BIGSERIAL PRIMARY KEY, name VARCHAR(150) NOT NULL, customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL',
    tax_id VARCHAR(50) UNIQUE, email VARCHAR(100), phone VARCHAR(50), address TEXT, city VARCHAR(100),
    postal_code VARCHAR(20), country VARCHAR(50), is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS sales_orders (
                                            id BIGSERIAL PRIMARY KEY, customer_id BIGINT, order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                            status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT', subtotal_amount NUMERIC(14, 2) DEFAULT 0.00,
    tax_amount NUMERIC(14, 2) DEFAULT 0.00, discount_amount NUMERIC(14, 2) DEFAULT 0.00,
    total_amount NUMERIC(14, 2) DEFAULT 0.00, payment_method VARCHAR(50), shipping_address TEXT,
    notes TEXT, created_by_user_id BIGINT, created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_so_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS sales_order_items (
                                                 id BIGSERIAL PRIMARY KEY, sales_order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
                                                 quantity NUMERIC(10, 3) NOT NULL, unit_price_at_sale NUMERIC(12, 2) NOT NULL,
    discount_per_item NUMERIC(12, 2) DEFAULT 0.00, subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_soi_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
    );

CREATE TABLE IF NOT EXISTS purchase_orders (
                                               id BIGSERIAL PRIMARY KEY, supplier_id BIGINT NOT NULL, order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                               expected_delivery_date DATE, status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(14, 2) DEFAULT 0.00, notes TEXT, created_by_user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS purchase_order_items (
                                                    id BIGSERIAL PRIMARY KEY, purchase_order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
                                                    quantity_ordered NUMERIC(10, 3) NOT NULL, quantity_received NUMERIC(10, 3) DEFAULT 0.000,
    unit_price NUMERIC(12, 2) NOT NULL, subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_poi_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
    );

CREATE TABLE IF NOT EXISTS stock_movements (
                                               id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL, movement_type VARCHAR(50) NOT NULL,
    quantity_changed NUMERIC(12, 3) NOT NULL, stock_after_movement NUMERIC(12, 3) NOT NULL,
    movement_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, reference_document_type VARCHAR(50),
    reference_document_id VARCHAR(100), notes TEXT, user_id BIGINT,
    CONSTRAINT fk_sm_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sm_user FOREIGN KEY (user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS product_recipes (
                                               id BIGSERIAL PRIMARY KEY,
    -- El producto final, 'padre' (ej: la Torta Marquise). Debe ser de tipo COMPOUND.
                                               main_product_id BIGINT NOT NULL,
    -- El insumo o ingrediente 'hijo' (ej: Chocolate). Debe ser de tipo PHYSICAL_GOOD.
                                               ingredient_product_id BIGINT NOT NULL,
    -- La cantidad de insumo necesaria para hacer UNA unidad del producto principal.
                                               quantity_required NUMERIC(10, 3) NOT NULL,

    CONSTRAINT fk_recipe_main_product FOREIGN KEY (main_product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient_product FOREIGN KEY (ingredient_product_id) REFERENCES products (id) ON DELETE RESTRICT,
    -- Nos aseguramos de que no se pueda añadir el mismo ingrediente dos veces a la misma receta.
    UNIQUE (main_product_id, ingredient_product_id)
    );

CREATE TABLE IF NOT EXISTS product_packaging (
                                                 id BIGSERIAL PRIMARY KEY,
    -- El producto principal que requiere empaque (ej: Torta)
                                                 main_product_id BIGINT NOT NULL,
    -- El producto que actúa como empaque (ej: Caja de Torta). Debe ser de tipo PHYSICAL_GOOD.
                                                 packaging_product_id BIGINT NOT NULL,
    -- La cantidad de empaque necesaria por cada unidad del producto principal.
                                                 quantity NUMERIC(10, 3) NOT NULL DEFAULT 1,

    CONSTRAINT fk_packaging_main_product FOREIGN KEY (main_product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_packaging_item_product FOREIGN KEY (packaging_product_id) REFERENCES products (id) ON DELETE RESTRICT,
    -- Evita que se añada el mismo empaque dos veces al mismo producto.
    UNIQUE (main_product_id, packaging_product_id)
    );

CREATE TABLE IF NOT EXISTS production_orders (
                                                 id BIGSERIAL PRIMARY KEY,
    -- El producto que se fabricó (debe ser de tipo COMPOUND)
                                                 product_id BIGINT NOT NULL,
    -- La cantidad que se fabricó en esta tanda
                                                 quantity_produced NUMERIC(12, 3) NOT NULL,
    -- La fecha en que se realizó la producción
    production_date DATE NOT NULL DEFAULT CURRENT_DATE,
    -- Notas adicionales sobre la producción (ej: "Lote para evento X")
    notes TEXT,
    -- El usuario que registró la producción
    created_by_user_id BIGINT,
    -- Timestamp de creación
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_po_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_pp_main_product_id ON product_packaging(main_product_id);

CREATE INDEX IF NOT EXISTS idx_pr_main_product_id ON product_recipes(main_product_id);

CREATE INDEX IF NOT EXISTS idx_sm_product_id ON stock_movements(product_id);


