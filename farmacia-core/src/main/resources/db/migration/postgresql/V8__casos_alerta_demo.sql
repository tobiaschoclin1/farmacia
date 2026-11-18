-- Casos de demostración para alertas (fechas relativas a la fecha de despliegue)
-- Out of Stock: Omeprazol 20mg
-- Low Stock: Paracetamol 500mg (min 80) y Lisinopril 10mg (min 60)
-- Expiring Soon: Vitamina C, Loratadina, Cetirizina

-- 1) Out of Stock: dejar en 0 todos los lotes de Omeprazol
UPDATE stock_lote SET cantidad_base = 0
WHERE lote_id IN (
  SELECT l.id FROM lotes l JOIN productos p ON p.id = l.producto_id
  WHERE p.codigo_barra = '7501001234571'
);

-- 2) Low Stock: vaciar lotes de Paracetamol y Lisinopril y cargar un lote con poco stock
UPDATE stock_lote SET cantidad_base = 0
WHERE lote_id IN (
  SELECT l.id FROM lotes l JOIN productos p ON p.id = l.producto_id
  WHERE p.codigo_barra IN ('7501001234568', '7501001234575')
);

INSERT INTO lotes (producto_id, fecha_lote, fecha_vencimiento)
SELECT p.id, CURRENT_DATE - 30, CURRENT_DATE + 180
FROM productos p
WHERE p.codigo_barra IN ('7501001234568', '7501001234575');

INSERT INTO stock_lote (lote_id, cantidad_base)
SELECT l.id,
       CASE WHEN p.codigo_barra = '7501001234568' THEN 25 ELSE 15 END
FROM lotes l JOIN productos p ON p.id = l.producto_id
WHERE p.codigo_barra IN ('7501001234568', '7501001234575')
  AND l.fecha_vencimiento = CURRENT_DATE + 180
  AND l.id NOT IN (SELECT lote_id FROM stock_lote);

-- 3) Expiring Soon: lotes que vencen en 5, 12 y 25 días
INSERT INTO lotes (producto_id, fecha_lote, fecha_vencimiento)
SELECT p.id, CURRENT_DATE - 60, CURRENT_DATE + v.dias
FROM (VALUES ('7501001234570', 12, 40),
             ('7501001234572', 25, 30),
             ('7501001234581', 5, 20)) AS v(codigo, dias, cant)
JOIN productos p ON p.codigo_barra = v.codigo;

INSERT INTO stock_lote (lote_id, cantidad_base)
SELECT l.id, v.cant
FROM (VALUES ('7501001234570', 12, 40),
             ('7501001234572', 25, 30),
             ('7501001234581', 5, 20)) AS v(codigo, dias, cant)
JOIN productos p ON p.codigo_barra = v.codigo
JOIN lotes l ON l.producto_id = p.id AND l.fecha_vencimiento = CURRENT_DATE + v.dias
WHERE l.id NOT IN (SELECT lote_id FROM stock_lote);
