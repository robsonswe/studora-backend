-- ==========================================
-- V9: Add Autoral field to Questao
-- Makes concurso_id nullable and adds autoral column
-- Uses SQLite "recreate table" pattern
-- ==========================================

-- 1. Create the new table with updated schema
CREATE TABLE questao_new (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_id     INTEGER REFERENCES concurso(id) ON DELETE CASCADE, -- now nullable
    enunciado       TEXT    NOT NULL,
    anulada         INTEGER NOT NULL DEFAULT 0,
    desatualizada   INTEGER NOT NULL DEFAULT 0,
    autoral         INTEGER NOT NULL DEFAULT 0,                        -- new field
    image_url       TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Migrate existing data; all existing rows are non-autoral
INSERT INTO questao_new (id, concurso_id, enunciado, anulada, desatualizada, autoral, image_url, created_at, updated_at)
    SELECT id, concurso_id, enunciado, anulada, desatualizada, 0, image_url, created_at, updated_at
    FROM questao;

-- 3. Drop old table and rename
DROP TABLE questao;
ALTER TABLE questao_new RENAME TO questao;

-- 4. Recreate all indexes (SQLite drops them with the original table)
CREATE INDEX idx_questao_concurso ON questao(concurso_id);
CREATE INDEX idx_questao_anulada  ON questao(anulada);
CREATE INDEX idx_questao_autoral  ON questao(autoral);

-- 5. Add include_autoral column to simulado table
ALTER TABLE simulado ADD COLUMN include_autoral INTEGER NOT NULL DEFAULT 0;
