-- =========================================================================
-- Add 'principal' flag to questao_subtema relationship
-- =========================================================================

-- SQLite doesn't support ALTER TABLE ADD COLUMN with PRIMARY KEY easily, 
-- and we want to add an 'id' column as well for the new middle entity.

CREATE TABLE questao_subtema_new (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id  INTEGER NOT NULL,
    subtema_id  INTEGER NOT NULL,
    principal   INTEGER NOT NULL DEFAULT 0,
    
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id),
    UNIQUE (questao_id, subtema_id)
);

-- Copy existing data
INSERT INTO questao_subtema_new (questao_id, subtema_id)
SELECT questao_id, subtema_id FROM questao_subtema;

-- Mark the first subtema of each question as principal to satisfy initial validation
UPDATE questao_subtema_new
SET principal = 1
WHERE id IN (
    SELECT MIN(id)
    FROM questao_subtema_new
    GROUP BY questao_id
);

-- Replace old table
DROP TABLE questao_subtema;
ALTER TABLE questao_subtema_new RENAME TO questao_subtema;

CREATE INDEX idx_questao_subtema_subtema ON questao_subtema (subtema_id);
CREATE INDEX idx_questao_subtema_principal ON questao_subtema (questao_id, principal);
