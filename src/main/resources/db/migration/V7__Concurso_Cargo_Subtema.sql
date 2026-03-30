-- V7: Concurso Cargo Subtema relationship
-- Links subtemas to specific concurso-cargo associations

CREATE TABLE IF NOT EXISTS concurso_cargo_subtema (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_cargo_id   INTEGER NOT NULL,
    subtema_id          INTEGER NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (concurso_cargo_id) REFERENCES concurso_cargo(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id),

    UNIQUE (concurso_cargo_id, subtema_id)
);

CREATE INDEX IF NOT EXISTS idx_concurso_cargo_subtema_concurso_cargo ON concurso_cargo_subtema (concurso_cargo_id);
