-- =================================================================
-- TABLAS ESPECÍFICAS PARA TENANTS DE INDUSTRIA GASTRONÓMICA
-- =================================================================


CREATE TABLE IF NOT EXISTS product_recipes (
                                               id BIGSERIAL PRIMARY KEY,
                                               main_product_id BIGINT NOT NULL,
                                               ingredient_product_id BIGINT NOT NULL,
                                               quantity_required NUMERIC(19, 6) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,             -- NUEVO CAMPO

    CONSTRAINT fk_recipe_main_product
    FOREIGN KEY (main_product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient_product
    FOREIGN KEY (ingredient_product_id) REFERENCES products (id) ON DELETE RESTRICT,

    -- Evita duplicar el mismo ingrediente en la misma receta
    UNIQUE (main_product_id, ingredient_product_id),

    -- Validaciones básicas
    CONSTRAINT chk_recipe_uom_allowed
    CHECK (unit_of_measure IN ('UNIT','KG','G','L','ML','CM3','PERCENTAGE')),
    CONSTRAINT chk_recipe_qty_positive
    CHECK (quantity_required > 0),
    -- Si es porcentaje, debe estar entre 0 y 1 (fracción)
    CONSTRAINT chk_recipe_pct_range
    CHECK (
              unit_of_measure <> 'PERCENTAGE'
              OR (quantity_required > 0 AND quantity_required <= 1)
    )
    );

CREATE TABLE IF NOT EXISTS product_packaging (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 main_product_id BIGINT NOT NULL,
                                                 packaging_product_id BIGINT NOT NULL,
                                                 quantity NUMERIC(10, 3) NOT NULL DEFAULT 1,
    CONSTRAINT fk_packaging_main_product FOREIGN KEY (main_product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_packaging_item_product FOREIGN KEY (packaging_product_id) REFERENCES products (id) ON DELETE RESTRICT,
    UNIQUE (main_product_id, packaging_product_id)
    );

CREATE TABLE IF NOT EXISTS production_orders (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 product_id BIGINT NOT NULL,
                                                 quantity_produced NUMERIC(12, 3) NOT NULL,
    production_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_order_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_prod_order_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS event_orders (
                                            id BIGSERIAL PRIMARY KEY, customer_id BIGINT, event_date DATE NOT NULL,
                                            status VARCHAR(30) NOT NULL, notes TEXT, total_amount NUMERIC(14, 2) DEFAULT 0.00,
    delivery_address TEXT, created_by_user_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eo_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_eo_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS event_order_items (
                                                 id BIGSERIAL PRIMARY KEY, event_order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
                                                 quantity NUMERIC(10, 3) NOT NULL, unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(14, 2) NOT NULL,
    CONSTRAINT fk_eoi_event_order FOREIGN KEY (event_order_id) REFERENCES event_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_eoi_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
    );