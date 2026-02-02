-- =========================
-- INSTITUIÇÃO
-- =========================
CREATE TABLE IF NOT EXISTS instituicao (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT COLLATE NOCASE NOT NULL UNIQUE,
    area        TEXT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- BANCA
-- =========================
CREATE TABLE IF NOT EXISTS banca (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT COLLATE NOCASE NOT NULL UNIQUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- CONCURSO 
-- =========================
CREATE TABLE IF NOT EXISTS concurso (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    instituicao_id  INTEGER NOT NULL,
    banca_id        INTEGER NOT NULL,
    ano             INTEGER NOT NULL,
    mes             INTEGER NOT NULL,
    edital          TEXT COLLATE NOCASE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (instituicao_id) REFERENCES instituicao(id),
    FOREIGN KEY (banca_id) REFERENCES banca(id),

    UNIQUE (instituicao_id, banca_id, ano, mes)
);

CREATE INDEX IF NOT EXISTS idx_concurso_instituicao ON concurso (instituicao_id);
CREATE INDEX IF NOT EXISTS idx_concurso_banca ON concurso (banca_id);
CREATE INDEX IF NOT EXISTS idx_concurso_ano ON concurso (ano);
CREATE INDEX IF NOT EXISTS idx_concurso_mes ON concurso (mes);

-- =========================
-- CARGO
-- =========================
CREATE TABLE IF NOT EXISTS cargo (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT COLLATE NOCASE NOT NULL,
    nivel       TEXT NOT NULL CHECK (nivel IN ('FUNDAMENTAL', 'MEDIO', 'SUPERIOR')),
    area        TEXT COLLATE NOCASE NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (nome, nivel, area)
);

-- =========================
-- CONCURSO ↔ CARGO
-- =========================
CREATE TABLE IF NOT EXISTS concurso_cargo (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_id     INTEGER NOT NULL,
    cargo_id        INTEGER NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (concurso_id) REFERENCES concurso(id) ON DELETE CASCADE,
    FOREIGN KEY (cargo_id) REFERENCES cargo(id),

    UNIQUE (concurso_id, cargo_id)
);

CREATE INDEX IF NOT EXISTS idx_concurso_cargo_concurso ON concurso_cargo (concurso_id);
CREATE INDEX IF NOT EXISTS idx_concurso_cargo_cargo ON concurso_cargo (cargo_id);

-- =========================
-- DISCIPLINA
-- =========================
CREATE TABLE IF NOT EXISTS disciplina (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nome        TEXT COLLATE NOCASE NOT NULL UNIQUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- TEMA
-- =========================
CREATE TABLE IF NOT EXISTS tema (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    disciplina_id   INTEGER NOT NULL,
    nome            TEXT COLLATE NOCASE NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (disciplina_id) REFERENCES disciplina(id),
    UNIQUE (disciplina_id, nome)
);

CREATE INDEX IF NOT EXISTS idx_tema_disciplina ON tema (disciplina_id);

-- =========================
-- SUBTEMA
-- =========================
CREATE TABLE IF NOT EXISTS subtema (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tema_id     INTEGER NOT NULL,
    nome        TEXT COLLATE NOCASE NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tema_id) REFERENCES tema(id),
    UNIQUE (tema_id, nome)
);

CREATE INDEX IF NOT EXISTS idx_subtema_tema ON subtema (tema_id);

-- =========================
-- QUESTÃO
-- =========================
CREATE TABLE IF NOT EXISTS questao (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_id     INTEGER NOT NULL,
    enunciado       TEXT NOT NULL,
    anulada         INTEGER NOT NULL DEFAULT 0,
    image_url       TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (concurso_id) REFERENCES concurso(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_questao_concurso ON questao (concurso_id);
CREATE INDEX IF NOT EXISTS idx_questao_anulada ON questao (anulada);

-- =========================
-- QUESTÃO ↔ SUBTEMA
-- =========================
CREATE TABLE IF NOT EXISTS questao_subtema (
    questao_id  INTEGER NOT NULL,
    subtema_id  INTEGER NOT NULL,

    PRIMARY KEY (questao_id, subtema_id),

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id)
);

CREATE INDEX IF NOT EXISTS idx_questao_subtema_subtema ON questao_subtema (subtema_id);

-- =========================
-- QUESTÃO ↔ CARGO
-- =========================
CREATE TABLE IF NOT EXISTS questao_cargo (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id          INTEGER NOT NULL,
    concurso_cargo_id   INTEGER NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (concurso_cargo_id) REFERENCES concurso_cargo(id) ON DELETE CASCADE,
    
    UNIQUE (questao_id, concurso_cargo_id)
);

CREATE INDEX IF NOT EXISTS idx_questao_cargo_concurso_cargo ON questao_cargo (concurso_cargo_id);

-- =========================
-- ALTERNATIVA
-- =========================
CREATE TABLE IF NOT EXISTS alternativa (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id      INTEGER NOT NULL,
    ordem           INTEGER NOT NULL,
    texto           TEXT NOT NULL,
    correta         INTEGER NOT NULL,
    justificativa   TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    UNIQUE (questao_id, ordem)
);

CREATE INDEX IF NOT EXISTS idx_alternativa_questao ON alternativa (questao_id);
CREATE INDEX IF NOT EXISTS idx_alternativa_correta ON alternativa (questao_id, correta);

-- =========================
-- RESPOSTA
-- =========================
CREATE TABLE IF NOT EXISTS resposta (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id      INTEGER NOT NULL UNIQUE,
    alternativa_id  INTEGER NOT NULL,
    respondida_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (alternativa_id) REFERENCES alternativa(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resposta_alternativa ON resposta (alternativa_id);
CREATE INDEX IF NOT EXISTS idx_resposta_data ON resposta (respondida_em);
