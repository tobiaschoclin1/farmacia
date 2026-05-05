-- V3: historial de movimientos de stock
-- En PostgreSQL la tabla ya existe desde V1, solo agregamos índices adicionales si no existen

-- Recrear tabla con estructura mejorada si es necesario
DROP TABLE IF EXISTS movimientos_stock CASCADE;

CREATE TABLE movimientos_stock (
  id            SERIAL PRIMARY KEY,
  tipo          VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
  usuario       VARCHAR(255),
  producto_id   INTEGER,
  lote_id       INTEGER NOT NULL,
  cantidad_base INTEGER NOT NULL,
  motivo        TEXT,
  fecha_hora    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL,
  FOREIGN KEY (lote_id)     REFERENCES lotes(id)     ON DELETE CASCADE
);

CREATE INDEX idx_movimientos_lote  ON movimientos_stock(lote_id);
CREATE INDEX idx_movimientos_prod  ON movimientos_stock(producto_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_stock(fecha_hora);
