-- =========================
-- INSTITUIÇÃO
-- =========================
CREATE TABLE instituicao (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL UNIQUE
);

-- =========================
-- BANCA
-- =========================
CREATE TABLE banca (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL UNIQUE
);

-- =========================
-- CONCURSO 
-- =========================
CREATE TABLE concurso (
    id              INTEGER PRIMARY KEY,
    instituicao_id  INTEGER NOT NULL,
    banca_id        INTEGER NOT NULL,
    ano             INTEGER NOT NULL,

    FOREIGN KEY (instituicao_id) REFERENCES instituicao(id),
    FOREIGN KEY (banca_id) REFERENCES banca(id),

    UNIQUE (instituicao_id, banca_id, ano)
);

CREATE INDEX idx_concurso_instituicao
    ON concurso (instituicao_id);

CREATE INDEX idx_concurso_banca
    ON concurso (banca_id);

CREATE INDEX idx_concurso_ano
    ON concurso (ano);

-- =========================
-- CARGO
-- =========================
CREATE TABLE cargo (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL,
    nivel   TEXT NOT NULL,
    area    TEXT NOT NULL,

    UNIQUE (nome, nivel, area)
);

-- =========================
-- CONCURSO ↔ CARGO
-- =========================
CREATE TABLE concurso_cargo (
    id              INTEGER PRIMARY KEY,
    concurso_id     INTEGER NOT NULL,
    cargo_id        INTEGER NOT NULL,

    FOREIGN KEY (concurso_id) REFERENCES concurso(id),
    FOREIGN KEY (cargo_id) REFERENCES cargo(id),

    UNIQUE (concurso_id, cargo_id)
);

CREATE INDEX idx_concurso_cargo_concurso
    ON concurso_cargo (concurso_id);

CREATE INDEX idx_concurso_cargo_cargo
    ON concurso_cargo (cargo_id);

-- =========================
-- DISCIPLINA
-- =========================
CREATE TABLE disciplina (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL UNIQUE
);

-- =========================
-- TEMA
-- =========================
CREATE TABLE tema (
    id              INTEGER PRIMARY KEY,
    disciplina_id   INTEGER NOT NULL,
    nome            TEXT NOT NULL,

    FOREIGN KEY (disciplina_id) REFERENCES disciplina(id),
    UNIQUE (disciplina_id, nome)
);

CREATE INDEX idx_tema_disciplina
    ON tema (disciplina_id);

-- =========================
-- SUBTEMA
-- =========================
CREATE TABLE subtema (
    id          INTEGER PRIMARY KEY,
    tema_id     INTEGER NOT NULL,
    nome        TEXT NOT NULL,

    FOREIGN KEY (tema_id) REFERENCES tema(id),
    UNIQUE (tema_id, nome)
);

CREATE INDEX idx_subtema_tema
    ON subtema (tema_id);

-- =========================
-- QUESTÃO
-- =========================
CREATE TABLE questao (
    id              INTEGER PRIMARY KEY,
    concurso_id     INTEGER NOT NULL,
    enunciado       TEXT NOT NULL,
    anulada         INTEGER NOT NULL DEFAULT 0,
    image_url       TEXT,

    FOREIGN KEY (concurso_id) REFERENCES concurso(id)
);

CREATE INDEX idx_questao_concurso
    ON questao (concurso_id);

CREATE INDEX idx_questao_anulada
    ON questao (anulada);

-- =========================
-- QUESTÃO ↔ SUBTEMA
-- =========================
CREATE TABLE questao_subtema (
    questao_id  INTEGER NOT NULL,
    subtema_id  INTEGER NOT NULL,

    PRIMARY KEY (questao_id, subtema_id),

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id)
);

CREATE INDEX idx_questao_subtema_subtema
    ON questao_subtema (subtema_id);

-- =========================
-- QUESTÃO ↔ CARGO
-- =========================
CREATE TABLE questao_cargo (
    id                  INTEGER PRIMARY KEY,
    questao_id          INTEGER NOT NULL,
    concurso_cargo_id   INTEGER NOT NULL,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (concurso_cargo_id) REFERENCES concurso_cargo(id),
    
    UNIQUE (questao_id, concurso_cargo_id) -- Move the uniqueness here
);

CREATE INDEX idx_questao_cargo_concurso_cargo
    ON questao_cargo (concurso_cargo_id);

-- =========================
-- ALTERNATIVA
-- =========================
CREATE TABLE alternativa (
    id              INTEGER PRIMARY KEY,
    questao_id      INTEGER NOT NULL,
    ordem           INTEGER NOT NULL,
    texto           TEXT NOT NULL,
    correta         INTEGER NOT NULL,
    justificativa   TEXT,

    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    UNIQUE (questao_id, ordem)
);

CREATE INDEX idx_alternativa_questao
    ON alternativa (questao_id);

CREATE INDEX idx_alternativa_correta
    ON alternativa (questao_id, correta);




-- =========================
-- RESPOSTA
-- =========================
CREATE TABLE resposta (
    id              INTEGER PRIMARY KEY,
    questao_id      INTEGER NOT NULL,
    alternativa_id  INTEGER NOT NULL,
    respondida_em   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (questao_id) REFERENCES questao(id),
    FOREIGN KEY (alternativa_id) REFERENCES alternativa(id)
);

CREATE INDEX idx_resposta_questao
    ON resposta (questao_id);

CREATE INDEX idx_resposta_alternativa
    ON resposta (alternativa_id);

CREATE INDEX idx_resposta_data
    ON resposta (respondida_em);
