-- Arreglar tabla usuarios si existe con estructura incorrecta
-- Eliminar la tabla existente y recrearla correctamente

DROP TABLE IF EXISTS usuarios CASCADE;

-- Crear tabla usuarios con la estructura correcta
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'USUARIO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP
);

-- Índices
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);

-- Usuario administrador por defecto (password: admin123)
-- Hash SHA-256 de "admin123" en Base64
INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion)
VALUES ('Administrador', 'admin@farmacia.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMINISTRADOR', TRUE, CURRENT_TIMESTAMP);
