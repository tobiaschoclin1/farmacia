-- Arreglar tabla usuarios si existe con estructura incorrecta
-- Eliminar la tabla existente y recrearla correctamente

DROP TABLE IF EXISTS usuarios;

-- Crear tabla usuarios con la estructura correcta
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    rol TEXT NOT NULL DEFAULT 'USUARIO',
    activo INTEGER NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP
);

-- Índices
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);

-- Usuario administrador por defecto (password: admin123)
-- Hash SHA-256 de "admin123" en Base64
INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion)
VALUES ('Administrador', 'admin@farmacia.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMINISTRADOR', 1, CURRENT_TIMESTAMP);
