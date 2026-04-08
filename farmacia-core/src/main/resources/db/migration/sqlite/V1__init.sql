CREATE TABLE IF NOT EXISTS usuarios (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  rol TEXT NOT NULL CHECK (rol IN ('ADMIN','OPERADOR')),
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS productos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo_barra TEXT UNIQUE,
  nombre TEXT NOT NULL,
  unidad_base TEXT NOT NULL DEFAULT 'TABLETA', -- TABLETA|UNI|ML|GR
  unidades_por_caja INTEGER,                   -- NULL si no aplica
  stock_minimo INTEGER NOT NULL DEFAULT 0,
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS lotes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  producto_id INTEGER NOT NULL,
  numero_lote TEXT,              -- quedó por compatibilidad, se depreca en V2
  fecha_vencimiento TEXT,        -- ISO yyyy-MM-dd
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE IF NOT EXISTS stock_lote (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  lote_id INTEGER NOT NULL,
  cantidad_base INTEGER NOT NULL, -- en unidad_base
  FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

CREATE TABLE IF NOT EXISTS movimientos_stock (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  fecha TEXT NOT NULL, -- ISO
  tipo TEXT NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','AJUSTE')),
  producto_id INTEGER NOT NULL,
  lote_id INTEGER,
  cantidad_base INTEGER NOT NULL,
  motivo TEXT,
  usuario TEXT,
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

CREATE VIEW IF NOT EXISTS v_stock_producto AS
SELECT
  p.id AS producto_id,
  p.nombre,
  p.unidad_base,
  p.unidades_por_caja,
  COALESCE(SUM(sl.cantidad_base),0) AS cantidad_base_total,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN CAST((COALESCE(SUM(sl.cantidad_base),0) / p.unidades_por_caja) AS INT)
    ELSE NULL END AS cajas,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN COALESCE(SUM(sl.cantidad_base),0) % p.unidades_por_caja
    ELSE NULL END AS tabletas
FROM productos p
LEFT JOIN lotes l ON l.producto_id = p.id
LEFT JOIN stock_lote sl ON sl.lote_id = l.id
GROUP BY p.id;

CREATE INDEX IF NOT EXISTS idx_lotes_prod ON lotes(producto_id);
CREATE INDEX IF NOT EXISTS idx_stock_lote_lote ON stock_lote(lote_id);
CREATE INDEX IF NOT EXISTS idx_mov_prod ON movimientos_stock(producto_id);
