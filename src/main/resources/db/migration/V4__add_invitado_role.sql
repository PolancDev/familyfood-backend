-- V4__add_invitado_role.sql
-- Actualiza la restricción CHECK de la columna role en la tabla users
-- para incluir el valor 'INVITADO' además de 'ADMIN' y 'CONSUMER'.
-- Es posible que la restricción no exista (si se creó manualmente en BD),
-- por lo que usamos IF EXISTS para ser idempotentes.

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('INVITADO', 'ADMIN', 'CONSUMER'));
