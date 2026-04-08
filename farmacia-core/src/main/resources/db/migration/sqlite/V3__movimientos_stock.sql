-- V3: historial de movimientos de stock
-- Limpiar restos de intentos previos para evitar esquemas parciales
DROP INDEX IF EXISTS idx_movimientos_lote;
DROP INDEX IF EXISTS idx_movimientos_prod;
DROP INDEX IF EXISTS idx_movimientos_fecha;
DROP TABLE IF EXISTS movimientos_stock;

CREATE TABLE movimientos_stock (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  tipo          TEXT NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
  usuario       TEXT,
  producto_id   INTEGER,
  lote_id       INTEGER NOT NULL,
  cantidad_base INTEGER NOT NULL,
  motivo        TEXT,
  fecha_hora    TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL,
  FOREIGN KEY (lote_id)     REFERENCES lotes(id)     ON DELETE CASCADE
);

CREATE INDEX idx_movimientos_lote  ON movimientos_stock(lote_id);
CREATE INDEX idx_movimientos_prod  ON movimientos_stock(producto_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_stock(fecha_hora);
