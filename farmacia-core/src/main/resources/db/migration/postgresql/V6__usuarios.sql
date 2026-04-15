-- Tabla de usuarios para autenticación
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'USUARIO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
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
SELECT 'Administrador', 'admin@farmacia.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMINISTRADOR', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'admin@farmacia.com'
);
