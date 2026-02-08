-- Update 'simulado' table with filter fields
ALTER TABLE simulado ADD COLUMN banca_id INTEGER;
ALTER TABLE simulado ADD COLUMN cargo_id INTEGER;
ALTER TABLE simulado ADD COLUMN nivel TEXT;
ALTER TABLE simulado ADD COLUMN ignorar_respondidas INTEGER DEFAULT 0;

-- Tables for collections (to support @ElementCollection in JPA)
CREATE TABLE IF NOT EXISTS simulado_area (
    simulado_id INTEGER NOT NULL,
    area TEXT NOT NULL,
    FOREIGN KEY (simulado_id) REFERENCES simulado(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS simulado_disciplina (
    simulado_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,
    FOREIGN KEY (simulado_id) REFERENCES simulado(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS simulado_tema (
    simulado_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,
    FOREIGN KEY (simulado_id) REFERENCES simulado(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS simulado_subtema (
    simulado_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    quantidade INTEGER NOT NULL,
    FOREIGN KEY (simulado_id) REFERENCES simulado(id) ON DELETE CASCADE
);
