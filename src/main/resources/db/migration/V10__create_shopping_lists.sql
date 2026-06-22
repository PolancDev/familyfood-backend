-- V10__create_shopping_lists.sql
-- Tablas para la lista de la compra (FamilyFood)

CREATE TABLE IF NOT EXISTS shopping_lists (
    id UUID PRIMARY KEY,
    family_group_id UUID NOT NULL,
    year INT NOT NULL,
    week_number INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_shopping_list_family FOREIGN KEY (family_group_id) REFERENCES family_groups(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_shopping_list_family_week ON shopping_lists(family_group_id, year, week_number);
CREATE INDEX IF NOT EXISTS idx_shopping_list_family ON shopping_lists(family_group_id);

CREATE TABLE IF NOT EXISTS shopping_items (
    id UUID PRIMARY KEY,
    shopping_list_id UUID NOT NULL,
    ingrediente VARCHAR(255) NOT NULL,
    cantidad DOUBLE PRECISION NOT NULL,
    unidad VARCHAR(50) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    comprado BOOLEAN NOT NULL DEFAULT FALSE,
    es_manual BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_shopping_item_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_shopping_item_list ON shopping_items(shopping_list_id);
