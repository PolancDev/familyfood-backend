-- V3__fix_family_tables.sql
-- Repara la creación de tablas familiares en caso de que V2 no se haya ejecutado
-- por checksum mismatch. Usa IF NOT EXISTS para ser seguro en ambos escenarios.

CREATE TABLE IF NOT EXISTS family_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS family_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    family_group_id UUID NOT NULL REFERENCES family_groups(id),
    role VARCHAR(20) NOT NULL DEFAULT 'CONSUMER',
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, family_group_id)
);

CREATE TABLE IF NOT EXISTS join_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    family_group_id UUID NOT NULL REFERENCES family_groups(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, family_group_id)
);

CREATE INDEX IF NOT EXISTS idx_family_members_user ON family_members(user_id);
CREATE INDEX IF NOT EXISTS idx_family_members_group ON family_members(family_group_id);
CREATE INDEX IF NOT EXISTS idx_join_requests_group ON join_requests(family_group_id);
CREATE INDEX IF NOT EXISTS idx_join_requests_status ON join_requests(status);
