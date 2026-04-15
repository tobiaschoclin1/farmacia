-- Tabla de usuarios para autenticación
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL DEFAULT 'USUARIO',
    activo INTEGER NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP
);

-- Índice para búsqueda por email
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- Índice para búsqueda por rol
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);

-- Usuario administrador por defecto (password: admin123)
-- Hash SHA-256 de "admin123" en Base64
-- Solo insertar si no existe
INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion)
SELECT 'Administrador', 'admin@farmacia.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMINISTRADOR', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'admin@farmacia.com'
);
