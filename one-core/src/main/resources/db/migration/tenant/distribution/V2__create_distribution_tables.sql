-- Define los almacenes o depósitos físicos del tenant.
CREATE TABLE IF NOT EXISTS warehouses (
                                          id BIGSERIAL PRIMARY KEY,
                                          name VARCHAR(150) NOT NULL UNIQUE,
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE
    );

-- Esta tabla reemplaza a los campos de stock en la tabla 'products'.
-- Guarda la cantidad de un producto en un almacén específico.
CREATE TABLE IF NOT EXISTS inventory_levels (
                                                id BIGSERIAL PRIMARY KEY,
                                                product_id BIGINT NOT NULL,
                                                warehouse_id BIGINT NOT NULL,
                                                quantity NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    stock_status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE', -- Podría ser AVAILABLE, FROZEN, QUALITY_CONTROL
    CONSTRAINT fk_inv_level_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_level_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE RESTRICT,
    UNIQUE (product_id, warehouse_id, stock_status)
    );

-- Registra cada envío, ya sea de entrada o de salida.
CREATE TABLE IF NOT EXISTS shipments (
                                         id BIGSERIAL PRIMARY KEY,
                                         direction VARCHAR(20) NOT NULL, -- INBOUND (entrada) o OUTBOUND (salida)
    status VARCHAR(50) NOT NULL, -- PREPARING, IN_TRANSIT, DELIVERED, CANCELLED
    tracking_number VARCHAR(100),
    carrier VARCHAR(100), -- ej: DHL, Maersk, Correo Argentino
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    -- Se puede vincular a una orden de compra o de venta
    purchase_order_id BIGINT UNIQUE,
    sales_order_id BIGINT UNIQUE,
    CONSTRAINT fk_shipment_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_shipment_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id)
    );


-- Define las diferentes listas de precios que maneja el negocio.
CREATE TABLE IF NOT EXISTS price_lists (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
    );

-- Vincula un producto a una lista con un precio específico.
CREATE TABLE IF NOT EXISTS price_list_items (
                                                id BIGSERIAL PRIMARY KEY,
                                                price_list_id BIGINT NOT NULL,
                                                product_id BIGINT NOT NULL,
                                                price NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_pli_price_list FOREIGN KEY (price_list_id) REFERENCES price_lists(id) ON DELETE CASCADE,
    CONSTRAINT fk_pli_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (price_list_id, product_id)
    );

-- Necesitaríamos añadir una columna a la tabla 'customers'.
 ALTER TABLE customers ADD COLUMN price_list_id BIGINT;
 ALTER TABLE customers ADD CONSTRAINT fk_customer_price_list FOREIGN KEY (price_list_id) REFERENCES price_lists(id);