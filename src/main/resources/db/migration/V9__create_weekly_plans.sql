-- V9__create_weekly_plans.sql
-- Tablas para el planificador semanal (FamilyFood)

CREATE TABLE IF NOT EXISTS weekly_plans (
    id UUID PRIMARY KEY,
    family_group_id UUID NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE,
    year INT NOT NULL,
    week_number INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_weekly_plan UNIQUE(family_group_id, year, week_number)
);

CREATE TABLE IF NOT EXISTS plan_days (
    id UUID PRIMARY KEY,
    weekly_plan_id UUID NOT NULL REFERENCES weekly_plans(id) ON DELETE CASCADE,
    dia VARCHAR(10) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    receta_id UUID REFERENCES recipes(id) ON DELETE SET NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'IMPROVISADO',
    sobras_origen_dia VARCHAR(10),
    sobras_origen_tipo VARCHAR(10),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_dia CHECK (dia IN ('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO')),
    CONSTRAINT ck_tipo CHECK (tipo IN ('COMIDA','CENA')),
    CONSTRAINT ck_estado CHECK (estado IN ('NORMAL','SOBRAS','COMER_FUERA','IMPROVISADO')),
    CONSTRAINT uq_plan_day UNIQUE(weekly_plan_id, dia, tipo)
);

CREATE INDEX IF NOT EXISTS idx_weekly_plans_family ON weekly_plans(family_group_id);
CREATE INDEX IF NOT EXISTS idx_weekly_plans_week ON weekly_plans(family_group_id, year, week_number);
CREATE INDEX IF NOT EXISTS idx_plan_days_plan ON plan_days(weekly_plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_days_receta ON plan_days(receta_id);
