-- V8__add_family_group_id_to_recipes.sql
-- Añade columna family_group_id a la tabla recipes para compartir recetas dentro del grupo familiar

ALTER TABLE recipes
ADD COLUMN family_group_id UUID NULL REFERENCES family_groups(id) ON DELETE SET NULL;

-- Índice para buscar recetas por grupo familiar
CREATE INDEX IF NOT EXISTS idx_recipes_family_group_id ON recipes(family_group_id);

-- Índice compuesto para búsquedas frecuentes por grupo familiar + nombre
CREATE INDEX IF NOT EXISTS idx_recipes_family_group_id_nombre ON recipes(family_group_id, nombre);

-- Migrar recetas existentes: asignar family_group_id basado en el grupo familiar del usuario
-- (Solo si el usuario pertenece a un grupo familiar)
UPDATE recipes r
SET family_group_id = fm.family_group_id
FROM family_members fm
WHERE r.user_id = fm.user_id
  AND fm.role = 'ADMIN'
  AND r.family_group_id IS NULL;