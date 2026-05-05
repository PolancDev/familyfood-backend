-- V6__create_recipes_table.sql
-- Tabla de recetas para FamilyFood

CREATE TABLE IF NOT EXISTS recipes (
    id UUID PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(2000) NOT NULL,
    tiempo_minutos INTEGER NOT NULL,
    raciones INTEGER NOT NULL,
    imagen VARCHAR(500),
    favorita BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_recipes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    recipe_id UUID NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    cantidad DOUBLE PRECISION NOT NULL,
    unidad VARCHAR(100) NOT NULL,
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipe_pasos (
    recipe_id UUID NOT NULL,
    paso VARCHAR(2000) NOT NULL,
    CONSTRAINT fk_recipe_pasos_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipe_etiquetas (
    recipe_id UUID NOT NULL,
    etiqueta VARCHAR(50) NOT NULL,
    CONSTRAINT fk_recipe_etiquetas_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_recipes_user_id ON recipes(user_id);
CREATE INDEX IF NOT EXISTS idx_recipes_favorita ON recipes(favorita);
CREATE INDEX IF NOT EXISTS idx_recipes_nombre ON recipes(nombre);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id);
CREATE INDEX IF NOT EXISTS idx_recipe_pasos_recipe_id ON recipe_pasos(recipe_id);
CREATE INDEX IF NOT EXISTS idx_recipe_etiquetas_recipe_id ON recipe_etiquetas(recipe_id);
