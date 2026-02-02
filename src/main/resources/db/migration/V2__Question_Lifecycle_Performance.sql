-- 1. Update 'questao' table
ALTER TABLE questao ADD COLUMN desatualizada BOOLEAN NOT NULL DEFAULT 0;

-- 2. Refactor 'resposta' table (Remove UNIQUE constraint and add new fields)
-- Since SQLite doesn't support DROP CONSTRAINT or DROP COLUMN well, we use the recreate pattern.

CREATE TABLE resposta_new (
    id                          INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id                  INTEGER NOT NULL,
    alternativa_id              INTEGER NOT NULL,
    justificativa               TEXT,
    dificuldade_id              INTEGER,
    tempo_resposta_segundos     INTEGER,
    created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (alternativa_id) REFERENCES alternativa(id) ON DELETE CASCADE
);

-- Copy old data
INSERT INTO resposta_new (id, questao_id, alternativa_id, created_at, updated_at)
SELECT id, questao_id, alternativa_id, created_at, updated_at FROM resposta;

DROP TABLE resposta;
ALTER TABLE resposta_new RENAME TO resposta;

CREATE INDEX idx_resposta_alternativa ON resposta (alternativa_id);
CREATE INDEX idx_resposta_questao ON resposta (questao_id);
CREATE INDEX idx_resposta_created_at ON resposta (created_at);