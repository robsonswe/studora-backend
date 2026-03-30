-- V6: Study Progress and Concurso Registration Tracking

-- Add inscrito column to concurso_cargo
ALTER TABLE concurso_cargo ADD COLUMN inscrito BOOLEAN NOT NULL DEFAULT FALSE;

-- Create estudo_subtema table
CREATE TABLE IF NOT EXISTS estudo_subtema (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    subtema_id  INTEGER NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_estudo_subtema_subtema FOREIGN KEY (subtema_id) REFERENCES subtema(id) ON DELETE CASCADE
);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_estudo_subtema_subtema ON estudo_subtema(subtema_id);
CREATE INDEX IF NOT EXISTS idx_estudo_subtema_created_at ON estudo_subtema(created_at);
