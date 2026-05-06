-- Agregar soporte para autenticación OAuth con Google
-- Agregar campo google_id a tabla usuarios
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);

-- Permitir password NULL para usuarios que se registran con Google
ALTER TABLE usuarios ALTER COLUMN password DROP NOT NULL;

-- Crear índice en google_id
CREATE INDEX IF NOT EXISTS idx_usuarios_google_id ON usuarios(google_id);
