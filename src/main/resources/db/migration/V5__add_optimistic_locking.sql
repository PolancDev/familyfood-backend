-- V5__add_optimistic_locking.sql
-- Añade columna version para control de concurrencia optimista (Optimistic Locking)
-- a todas las tablas del dominio.

ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE family_groups ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE family_members ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE join_requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
