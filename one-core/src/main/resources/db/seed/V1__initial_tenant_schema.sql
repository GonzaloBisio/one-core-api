-- =================================================================
-- SCRIPT PARA CREAR TABLAS Y DATOS DE PRUEBA EN EL SCHEMA tenant_empresa_test
-- Ejecutar con el schema 'tenant_empresa_test' como activo o prefijando todo.
-- Se asume que el schema 'tenant_empresa_test' ya existe.
-- Se asume que public.system_users tiene usuarios con id 1 y 2.
-- =================================================================

-- Asegurar que estamos en el contexto del schema correcto si no se prefija
-- (En psql: \c one-core-db; SET search_path TO tenant_empresa_test, public;)
-- Para DBeaver, asegúrate de que el script se ejecute contra el schema 'tenant_empresa_test'
-- o mantén los prefijos 'tenant_empresa_test.' en todos los nombres de tabla.

CREATE SCHEMA IF NOT EXISTS tenant_empresa_test;
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
                                                                      name VARCHAR(100) NOT NULL UNIQUE, -- Nombre único dentro del tenant
    description TEXT,
    parent_category_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pc_parent_category FOREIGN KEY (parent_category_id)
    REFERENCES tenant_empresa_test.product_categories (id) ON DELETE SET NULL
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
    created_by_user_id BIGINT, -- FK a public.system_users.id
    updated_by_user_id BIGINT, -- FK a public.system_users.id
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
    REFERENCES tenant_empresa_test.product_categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_default_supplier FOREIGN KEY (default_supplier_id)
    REFERENCES tenant_empresa_test.suppliers (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL, -- APUNTA A public.system_users
    CONSTRAINT fk_product_updated_by FOREIGN KEY (updated_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL  -- APUNTA A public.system_users
    );
COMMENT ON TABLE tenant_empresa_test.products IS 'Catálogo de productos/servicios que ofrece la empresa.';

-- -----------------------------------------------------
-- Tabla: stock_movements
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.stock_movements (
                                                                   id BIGSERIAL PRIMARY KEY,
                                                                   product_id BIGINT NOT NULL,
                                                                   movement_type VARCHAR(50) NOT NULL, -- Ej: INITIAL_STOCK, PURCHASE_RECEIPT, SALE_CONFIRMED, ADJUSTMENT_IN, ADJUSTMENT_OUT, WASTAGE
    quantity_changed NUMERIC(12, 3) NOT NULL,
    stock_after_movement NUMERIC(12, 3) NOT NULL,
    movement_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    reference_document_type VARCHAR(50),
    reference_document_id VARCHAR(100),
    notes TEXT,
    user_id BIGINT, -- FK a public.system_users.id
    CONSTRAINT fk_sm_product FOREIGN KEY (product_id)
    REFERENCES tenant_empresa_test.products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sm_user FOREIGN KEY (user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL -- APUNTA A public.system_users
    );
COMMENT ON TABLE tenant_empresa_test.stock_movements IS 'Historial de todos los movimientos de entrada y salida de stock.';
CREATE INDEX IF NOT EXISTS idx_sm_product_id ON tenant_empresa_test.stock_movements(product_id);

-- -----------------------------------------------------
-- Tabla: customers
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.customers (
                                                             id BIGSERIAL PRIMARY KEY,
                                                             name VARCHAR(150) NOT NULL,
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, COMPANY
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

-- -----------------------------------------------------
-- Tabla: sales_orders
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.sales_orders (
                                                                id BIGSERIAL PRIMARY KEY,
                                                                customer_id BIGINT,
                                                                order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                                                status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT', -- Ej: PENDING_PAYMENT, PROCESSING, SHIPPED, COMPLETED, CANCELLED
    subtotal_amount NUMERIC(14, 2) DEFAULT 0.00,
    tax_amount NUMERIC(14, 2) DEFAULT 0.00,
    discount_amount NUMERIC(14, 2) DEFAULT 0.00,
    total_amount NUMERIC(14, 2) DEFAULT 0.00,
    payment_method VARCHAR(50),
    shipping_address TEXT,
    notes TEXT,
    created_by_user_id BIGINT, -- FK a public.system_users.id
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id)
    REFERENCES tenant_empresa_test.customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_so_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE tenant_empresa_test.sales_orders IS 'Pedidos de venta o facturas emitidas a clientes.';

-- -----------------------------------------------------
-- Tabla: sales_order_items
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.sales_order_items (
                                                                     id BIGSERIAL PRIMARY KEY,
                                                                     sales_order_id BIGINT NOT NULL,
                                                                     product_id BIGINT NOT NULL,
                                                                     quantity NUMERIC(10, 3) NOT NULL,
    unit_price_at_sale NUMERIC(12, 2) NOT NULL,
    discount_per_item NUMERIC(12, 2) DEFAULT 0.00,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00, -- Calculado por la aplicación
    CONSTRAINT fk_soi_sales_order FOREIGN KEY (sales_order_id)
    REFERENCES tenant_empresa_test.sales_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_soi_product FOREIGN KEY (product_id)
    REFERENCES tenant_empresa_test.products (id) ON DELETE RESTRICT
    );
COMMENT ON TABLE tenant_empresa_test.sales_order_items IS 'Ítems detallados en cada pedido de venta o factura.';

-- -----------------------------------------------------
-- Tabla: purchase_orders
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.purchase_orders (
                                                                   id BIGSERIAL PRIMARY KEY,
                                                                   supplier_id BIGINT NOT NULL,
                                                                   order_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                                                   expected_delivery_date DATE,
                                                                   status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- Ej: DRAFT, ORDERED, PARTIALLY_RECEIVED, FULLY_RECEIVED, CANCELLED
    total_amount NUMERIC(14, 2) DEFAULT 0.00,
    notes TEXT,
    created_by_user_id BIGINT, -- FK a public.system_users.id
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id)
    REFERENCES tenant_empresa_test.suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_created_by FOREIGN KEY (created_by_user_id)
    REFERENCES public.system_users (id) ON DELETE SET NULL
    );
COMMENT ON TABLE tenant_empresa_test.purchase_orders IS 'Órdenes de compra realizadas a los proveedores.';

-- -----------------------------------------------------
-- Tabla: purchase_order_items
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_empresa_test.purchase_order_items (
                                                                        id BIGSERIAL PRIMARY KEY,
                                                                        purchase_order_id BIGINT NOT NULL,
                                                                        product_id BIGINT NOT NULL,
                                                                        quantity_ordered NUMERIC(10, 3) NOT NULL,
    quantity_received NUMERIC(10, 3) DEFAULT 0.000,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0.00, -- Calculado por la aplicación
    CONSTRAINT fk_poi_purchase_order FOREIGN KEY (purchase_order_id)
    REFERENCES tenant_empresa_test.purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id)
    REFERENCES tenant_empresa_test.products (id) ON DELETE RESTRICT
    );
COMMENT ON TABLE tenant_empresa_test.purchase_order_items IS 'Ítems detallados en cada orden de compra.';


-- =================================================================
-- DATOS DE PRUEBA PARA tenant_empresa_test
-- Asumimos que en public.system_users:
-- ID 1 = 'usuario@test.com' (STANDARD_USER)
-- ID 2 = 'admin@sistema.com' (SUPER_ADMIN)
-- =================================================================

-- Proveedores
INSERT INTO tenant_empresa_test.suppliers (id, name, contact_person, email, phone, address, tax_id, notes) VALUES
    (1, 'Proveedor Mayorista A', 'Juan Perez', 'contacto@proveedora.com', '11-1234-5678', 'Calle Falsa 123, Ciudad A', '30-12345678-1', 'Entrega los martes y jueves')
    ON CONFLICT (id) DO NOTHING;
INSERT INTO tenant_empresa_test.suppliers (id, name, contact_person, email, phone, tax_id) VALUES
    (2, 'Insumos del Sur SRL', 'Ana Gomez', 'ventas@insumosdelsur.com', '11-8765-4321', '33-87654321-9')
    ON CONFLICT (id) DO NOTHING;

-- Categorías de Productos
INSERT INTO tenant_empresa_test.product_categories (id, name, description) VALUES
    (1, 'Lácteos', 'Productos derivados de la leche')
    ON CONFLICT (id) DO NOTHING;
INSERT INTO tenant_empresa_test.product_categories (id, name, description, parent_category_id) VALUES
    (2, 'Yogures', 'Variedades de yogur', 1) -- Subcategoría de Lácteos
    ON CONFLICT (id) DO NOTHING;
INSERT INTO tenant_empresa_test.product_categories (id, name, description) VALUES
    (3, 'Ferretería', 'Herramientas y materiales de construcción')
    ON CONFLICT (id) DO NOTHING;
INSERT INTO tenant_empresa_test.product_categories (id, name, description, parent_category_id) VALUES
    (4, 'Tornillos', 'Todo tipo de tornillos', 3) -- Subcategoría de Ferretería
    ON CONFLICT (id) DO NOTHING;


-- Productos
-- Asumiendo que SystemUser con ID 1 ('usuario@test.com') crea estos productos
INSERT INTO tenant_empresa_test.products (id, sku, name, description, category_id, default_supplier_id, purchase_price, sale_price, unit_of_measure, current_stock, minimum_stock_level, is_active, created_by_user_id, updated_by_user_id) VALUES
                                                                                                                                                                                                                                                (101, 'LAC-YOG-FRUT-1L', 'Yogur Frutilla 1L', 'Yogur bebible sabor frutilla, botella 1 litro.', 2, 1, 80.50, 120.00, 'UNIT', 50.000, 10.000, TRUE, 1, 1),
                                                                                                                                                                                                                                                (102, 'FER-TOR-PH-001', 'Tornillo Phillips Cabeza Plana 3x20', 'Tornillo Phillips de acero zincado, cabeza plana, 3mm x 20mm. Caja x100.', 4, 2, 150.00, 250.00, 'BOX', 20.000, 5.000, TRUE, 1, 1),
                                                                                                                                                                                                                                                (103, 'LAC-LECHE-ENT-1L', 'Leche Entera Sachet 1L', 'Leche entera pasteurizada, sachet 1 litro.', 1, 1, 70.00, 100.00, 'UNIT', 100.000, 20.000, TRUE, 1, 1)
    ON CONFLICT (id) DO NOTHING;

-- Movimiento de Stock Inicial para los productos (ejemplo)
-- Asumimos que el SystemUser con ID 2 ('admin@sistema.com') registra el stock inicial
INSERT INTO tenant_empresa_test.stock_movements (product_id, movement_type, quantity_changed, stock_after_movement, notes, user_id) VALUES
                                                                                                                                        (101, 'INITIAL_STOCK', 50.000, 50.000, 'Stock inicial del producto Yogur Frutilla', 2),
                                                                                                                                        (102, 'INITIAL_STOCK', 20.000, 20.000, 'Stock inicial del producto Tornillos Phillips', 2),
                                                                                                                                        (103, 'INITIAL_STOCK', 100.000, 100.000, 'Stock inicial del producto Leche Entera', 2)
    ON CONFLICT (id) DO NOTHING; -- O una constraint más específica si quieres evitar duplicados lógicos

-- Clientes
INSERT INTO tenant_empresa_test.customers (id, name, customer_type, tax_id, email, phone, address, city, country) VALUES
                                                                                                                      (201, 'Consumidor Final Varios', 'INDIVIDUAL', '99999999999', 'cf@example.com', '000-0000000', 'Mostrador', 'Ciudad Local', 'Argentina'),
                                                                                                                      (202, 'Construcciones XYZ SRL', 'COMPANY', '30-98765432-1', 'compras@construccionesxyz.com', '11-5555-4444', 'Av. Siempreviva 742', 'Ciudad Local', 'Argentina')
    ON CONFLICT (id) DO NOTHING;

-- Ejemplo de una Orden de Venta (SalesOrder)
-- Asumimos que el SystemUser con ID 1 ('usuario@test.com') crea esta orden
INSERT INTO tenant_empresa_test.sales_orders (id, customer_id, order_date, status, payment_method, notes, created_by_user_id, subtotal_amount, tax_amount, discount_amount, total_amount) VALUES
    (301, 202, '2025-05-30', 'PROCESSING', 'TRANSFERENCIA', 'Pedido para obra nueva', 1, 290.00, 60.90, 0.00, 350.90)
    ON CONFLICT (id) DO NOTHING;

-- Ítems para la Orden de Venta 301
INSERT INTO tenant_empresa_test.sales_order_items (sales_order_id, product_id, quantity, unit_price_at_sale, discount_per_item, subtotal) VALUES
                                                                                                                                              (301, 101, 2.000, 120.00, 0.00, 240.00), -- 2 Yogures
                                                                                                                                              (301, 103, 0.500, 100.00, 0.00, 50.00)  -- Media Leche (asumiendo que vendes fracciones y tu stock lo permite)
    ON CONFLICT (id) DO NOTHING; -- O una constraint más específica (sales_order_id, product_id)

-- Movimientos de Stock para la Orden de Venta 301 (asumiendo que se procesó y el stock se descontó)
-- Aquí es donde tu InventoryService haría esto automáticamente al confirmar la orden.
-- Lo pongo manual para tener el dato, pero en la app no harías esto así.
INSERT INTO tenant_empresa_test.stock_movements (product_id, movement_type, quantity_changed, stock_after_movement, reference_document_type, reference_document_id, notes, user_id) VALUES
                                                                                                                                                                                        (101, 'SALE_CONFIRMED', -2.000, 48.000, 'SALES_ORDER', '301', 'Venta Orden 301', 1),
                                                                                                                                                                                        (103, 'SALE_CONFIRMED', -0.500, 99.500, 'SALES_ORDER', '301', 'Venta Orden 301', 1)
    ON CONFLICT (id) DO NOTHING;
-- Y ACTUALIZAR EL STOCK EN LA TABLA products CORRESPONDIENTE (LO HARÍA EL SERVICIO)
UPDATE tenant_empresa_test.products SET current_stock = 48.000 WHERE id = 101;
UPDATE tenant_empresa_test.products SET current_stock = 99.500 WHERE id = 103;


-- Ejemplo de una Orden de Compra (PurchaseOrder)
-- Asumimos que el SystemUser con ID 2 ('admin@sistema.com') crea esta orden
INSERT INTO tenant_empresa_test.purchase_orders (id, supplier_id, order_date, expected_delivery_date, status, notes, created_by_user_id, total_amount) VALUES
    (401, 1, '2025-06-01', '2025-06-10', 'ORDERED', 'Reposición de lácteos', 2, 8050.00)
    ON CONFLICT (id) DO NOTHING;

-- Ítems para la Orden de Compra 401
INSERT INTO tenant_empresa_test.purchase_order_items (purchase_order_id, product_id, quantity_ordered, unit_price, subtotal) VALUES
    (401, 101, 100.000, 80.50, 8050.00) -- 100 Yogures
    ON CONFLICT (id) DO NOTHING;
-- (No pongo el movimiento de stock aquí, ya que se haría al "recibir mercancía")
