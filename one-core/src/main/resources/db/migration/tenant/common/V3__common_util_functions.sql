-- Helpers comunes para todos los tenants
-- Crea la función has_packaging(product_id) que es segura aunque no exista product_packaging

CREATE OR REPLACE FUNCTION has_packaging(p_product_id BIGINT)
RETURNS BOOLEAN
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
reg_tbl regclass;
  has_it BOOLEAN := FALSE;
BEGIN
  -- ¿Existe la tabla product_packaging en ESTE schema de tenant?
SELECT to_regclass(current_schema() || '.product_packaging') INTO reg_tbl;

IF reg_tbl IS NULL THEN
    -- En industrias donde no existe (p.ej. GYM), devolvemos FALSE sin fallar
    RETURN FALSE;
END IF;

  -- Si existe, verificamos si hay filas para el producto
SELECT EXISTS (
    SELECT 1 FROM product_packaging WHERE main_product_id = p_product_id
) INTO has_it;

RETURN has_it;
END;
$$;
