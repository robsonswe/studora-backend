-- V17__Cleanup_Obsolete_Schema.sql
-- 1. Drop unused table
DROP TABLE IF EXISTS questao_cargo;

-- 2. Drop obsolete column concurso_id from questao
-- SQLite pattern for dropping a column: recreate the table.
CREATE TABLE questao_new (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    enunciado       TEXT    NOT NULL,
    anulada         INTEGER NOT NULL DEFAULT 0,
    desatualizada   INTEGER NOT NULL DEFAULT 0,
    autoral         INTEGER NOT NULL DEFAULT 0,
    image_url       TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO questao_new (id, enunciado, anulada, desatualizada, autoral, image_url, created_at, updated_at)
    SELECT id, enunciado, anulada, desatualizada, autoral, image_url, created_at, updated_at
    FROM questao;

DROP TABLE questao;
ALTER TABLE questao_new RENAME TO questao;

-- 3. Recreate indexes
CREATE INDEX idx_questao_anulada  ON questao(anulada);
CREATE INDEX idx_questao_autoral  ON questao(autoral);
