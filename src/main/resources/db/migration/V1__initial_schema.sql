-- V1__initial_schema.sql
-- Tabla de usuarios para FamilyFood

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'INVITADO',
    miembros_familia INTEGER,
    nivel_cocina VARCHAR(50),
    restricciones_alimentarias TEXT
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);