-- CONCURSO
CREATE TABLE concurso (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL,
    banca   TEXT NOT NULL,
    ano     INTEGER NOT NULL,
    cargo   TEXT,
    nivel   TEXT,
    area    TEXT
);

CREATE INDEX idx_concurso_banca_ano
    ON concurso (banca, ano);

-- DISCIPLINA
CREATE TABLE disciplina (
    id      INTEGER PRIMARY KEY,
    nome    TEXT NOT NULL UNIQUE
);

-- TEMA
CREATE TABLE tema (
    id              INTEGER PRIMARY KEY,
    disciplina_id   INTEGER NOT NULL,
    nome            TEXT NOT NULL,
    UNIQUE (disciplina_id, nome),
    FOREIGN KEY (disciplina_id) REFERENCES disciplina(id)
);

CREATE INDEX idx_tema_disciplina
    ON tema (disciplina_id);

-- SUBTEMA
CREATE TABLE subtema (
    id          INTEGER PRIMARY KEY,
    tema_id     INTEGER NOT NULL,
    nome        TEXT NOT NULL,
    UNIQUE (tema_id, nome),
    FOREIGN KEY (tema_id) REFERENCES tema(id)
);

CREATE INDEX idx_subtema_tema
    ON subtema (tema_id);

-- QUESTÃO
CREATE TABLE questao (
    id              INTEGER PRIMARY KEY,
    concurso_id     INTEGER NOT NULL,
    enunciado       TEXT NOT NULL,
    anulada         INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (concurso_id) REFERENCES concurso(id)
);

CREATE INDEX idx_questao_concurso
    ON questao (concurso_id);

CREATE INDEX idx_questao_anulada
    ON questao (anulada);

-- QUESTÃO ↔ SUBTEMA
CREATE TABLE questao_subtema (
    questao_id  INTEGER NOT NULL,
    subtema_id  INTEGER NOT NULL,
    PRIMARY KEY (questao_id, subtema_id),
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id)
);

CREATE INDEX idx_questao_subtema_subtema
    ON questao_subtema (subtema_id);

-- ALTERNATIVA
CREATE TABLE alternativa (
    id              INTEGER PRIMARY KEY,
    questao_id      INTEGER NOT NULL,
    ordem           INTEGER NOT NULL,
    texto           TEXT NOT NULL,
    correta         INTEGER NOT NULL,
    justificativa   TEXT,
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE
);

CREATE INDEX idx_alternativa_questao
    ON alternativa (questao_id);

CREATE INDEX idx_alternativa_ordem
    ON alternativa (questao_id, ordem);

CREATE INDEX idx_alternativa_correta
    ON alternativa (questao_id, correta);

-- IMAGEM
CREATE TABLE imagem (
    id          INTEGER PRIMARY KEY,
    url         TEXT NOT NULL,
    descricao   TEXT
);

-- QUESTÃO IMAGEM
CREATE TABLE questao_imagem (
    questao_id  INTEGER NOT NULL,
    imagem_id   INTEGER NOT NULL,
    PRIMARY KEY (questao_id, imagem_id),
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (imagem_id) REFERENCES imagem(id)
);

CREATE INDEX idx_questao_imagem_imagem
    ON questao_imagem (imagem_id);

-- ALTERNATIVA IMAGEM
CREATE TABLE alternativa_imagem (
    alternativa_id INTEGER NOT NULL,
    imagem_id      INTEGER NOT NULL,
    PRIMARY KEY (alternativa_id, imagem_id),
    FOREIGN KEY (alternativa_id) REFERENCES alternativa(id) ON DELETE CASCADE,
    FOREIGN KEY (imagem_id) REFERENCES imagem(id)
);

CREATE INDEX idx_alternativa_imagem_imagem
    ON alternativa_imagem (imagem_id);

-- RESPOSTA
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
