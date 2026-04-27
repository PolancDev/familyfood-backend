-- Crear tabla de historial de Flyway
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INTEGER NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL
);

-- Marcar V1 como aplicada (tabla users ya existe)
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES (1, '1', '<< Flyway Baseline >>', 'SQL', 'V1__initial_schema.sql', NULL, 'manual', 0, true);

-- Marcar V2 como aplicada si role ya existe
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES (2, '2', '<< Flyway Baseline >>', 'SQL', 'V2__add_role_to_users.sql', NULL, 'manual', 0, true);