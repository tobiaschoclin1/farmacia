-- Agrego fecha_lote (Date) y mantengo numero_lote por compatibilidad
ALTER TABLE lotes ADD COLUMN fecha_lote DATE;

-- Para datos existentes, seteo hoy si está null
UPDATE lotes SET fecha_lote = CURRENT_DATE WHERE fecha_lote IS NULL;

-- Vista de stock por lote (con conversión a cajas/tabletas)
DROP VIEW IF EXISTS v_stock_lote;
CREATE OR REPLACE VIEW v_stock_lote AS
SELECT
  p.id AS producto_id,
  p.nombre,
  p.unidad_base,
  p.unidades_por_caja,
  l.id AS lote_id,
  l.fecha_lote,
  l.fecha_vencimiento,
  COALESCE(sl.cantidad_base,0) AS cantidad_base,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN FLOOR(COALESCE(sl.cantidad_base,0) / p.unidades_por_caja)::INTEGER
    ELSE NULL END AS cajas,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN COALESCE(sl.cantidad_base,0) % p.unidades_por_caja
    ELSE NULL END AS tabletas
FROM productos p
JOIN lotes l ON l.producto_id = p.id
LEFT JOIN stock_lote sl ON sl.lote_id = l.id;
