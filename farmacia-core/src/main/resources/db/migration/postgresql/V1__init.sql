CREATE TABLE IF NOT EXISTS usuarios (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN','OPERADOR')),
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS productos (
  id SERIAL PRIMARY KEY,
  codigo_barra VARCHAR(255) UNIQUE,
  nombre VARCHAR(500) NOT NULL,
  unidad_base VARCHAR(50) NOT NULL DEFAULT 'TABLETA', -- TABLETA|UNI|ML|GR
  unidades_por_caja INTEGER,                   -- NULL si no aplica
  stock_minimo INTEGER NOT NULL DEFAULT 0,
  activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS lotes (
  id SERIAL PRIMARY KEY,
  producto_id INTEGER NOT NULL,
  numero_lote VARCHAR(255),              -- quedó por compatibilidad, se depreca en V2
  fecha_vencimiento DATE,                -- Date type en PostgreSQL
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stock_lote (
  id SERIAL PRIMARY KEY,
  lote_id INTEGER NOT NULL UNIQUE,       -- UNIQUE constraint para evitar duplicados
  cantidad_base INTEGER NOT NULL,        -- en unidad_base
  FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS movimientos_stock (
  id SERIAL PRIMARY KEY,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','AJUSTE')),
  producto_id INTEGER NOT NULL,
  lote_id INTEGER,
  cantidad_base INTEGER NOT NULL,
  motivo TEXT,
  usuario VARCHAR(255),
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

CREATE OR REPLACE VIEW v_stock_producto AS
SELECT
  p.id AS producto_id,
  p.nombre,
  p.unidad_base,
  p.unidades_por_caja,
  COALESCE(SUM(sl.cantidad_base),0) AS cantidad_base_total,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN FLOOR(COALESCE(SUM(sl.cantidad_base),0) / p.unidades_por_caja)::INTEGER
    ELSE NULL END AS cajas,
  CASE WHEN p.unidades_por_caja IS NOT NULL AND p.unidades_por_caja > 0
    THEN COALESCE(SUM(sl.cantidad_base),0) % p.unidades_por_caja
    ELSE NULL END AS tabletas
FROM productos p
LEFT JOIN lotes l ON l.producto_id = p.id
LEFT JOIN stock_lote sl ON sl.lote_id = l.id
GROUP BY p.id, p.nombre, p.unidad_base, p.unidades_por_caja;

CREATE INDEX IF NOT EXISTS idx_lotes_prod ON lotes(producto_id);
CREATE INDEX IF NOT EXISTS idx_stock_lote_lote ON stock_lote(lote_id);
CREATE INDEX IF NOT EXISTS idx_mov_prod ON movimientos_stock(producto_id);
CREATE INDEX IF NOT EXISTS idx_mov_fecha ON movimientos_stock(fecha);
