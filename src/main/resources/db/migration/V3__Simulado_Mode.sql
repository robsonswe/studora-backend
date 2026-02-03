-- 1. Create 'simulado' table
CREATE TABLE IF NOT EXISTS simulado (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nome            TEXT NOT NULL,
    started_at      DATETIME,
    finished_at     DATETIME,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create 'simulado_questao' join table (Snapshot of the exam)
CREATE TABLE IF NOT EXISTS simulado_questao (
    simulado_id     INTEGER NOT NULL,
    questao_id      INTEGER NOT NULL,
    ordem           INTEGER NOT NULL,

    PRIMARY KEY (simulado_id, questao_id),
    FOREIGN KEY (simulado_id) REFERENCES simulado(id) ON DELETE CASCADE,
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_simulado_questao_questao ON simulado_questao (questao_id);

-- 3. Update 'resposta' table to link to a simulado
ALTER TABLE resposta ADD COLUMN simulado_id INTEGER REFERENCES simulado(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_resposta_simulado ON resposta (simulado_id);
