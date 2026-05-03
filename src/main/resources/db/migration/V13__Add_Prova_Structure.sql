-- ==============================================================================
-- 1. CRIAÇÃO DAS TABELAS (NOVA ARQUITETURA DE PROVAS)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS prova (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_id     INTEGER NOT NULL,
    nome            TEXT    NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (concurso_id) REFERENCES concurso(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_prova_concurso ON prova(concurso_id);

CREATE TABLE IF NOT EXISTS prova_cargo (
    prova_id          INTEGER NOT NULL,
    concurso_cargo_id INTEGER NOT NULL,
    
    PRIMARY KEY (prova_id, concurso_cargo_id),
    FOREIGN KEY (prova_id) REFERENCES prova(id) ON DELETE CASCADE,
    FOREIGN KEY (concurso_cargo_id) REFERENCES concurso_cargo(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_prova_cargo_concurso_cargo ON prova_cargo(concurso_cargo_id);

CREATE TABLE IF NOT EXISTS prova_secao (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    prova_id     INTEGER NOT NULL,
    nome         TEXT    NOT NULL,
    ordem        INTEGER NOT NULL,
    num_questoes INTEGER,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prova_id) REFERENCES prova(id) ON DELETE CASCADE,
    UNIQUE(prova_id, ordem)
);

CREATE INDEX IF NOT EXISTS idx_prova_secao_prova ON prova_secao(prova_id);

CREATE TABLE IF NOT EXISTS prova_secao_peso (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    prova_secao_id      INTEGER NOT NULL,
    concurso_cargo_id   INTEGER,
    peso                REAL    NOT NULL DEFAULT 1.0,
    nota_minima         REAL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prova_secao_id) REFERENCES prova_secao(id) ON DELETE CASCADE,
    FOREIGN KEY (concurso_cargo_id) REFERENCES concurso_cargo(id) ON DELETE CASCADE,
    UNIQUE(prova_secao_id, concurso_cargo_id)
);

CREATE INDEX IF NOT EXISTS idx_prova_secao_peso_secao ON prova_secao_peso(prova_secao_id);
CREATE INDEX IF NOT EXISTS idx_prova_secao_peso_cargo ON prova_secao_peso(concurso_cargo_id);

-- O Edital: vincula subtemas à seção da prova
CREATE TABLE IF NOT EXISTS prova_secao_subtema (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    prova_secao_id  INTEGER NOT NULL,
    subtema_id      INTEGER NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prova_secao_id) REFERENCES prova_secao(id) ON DELETE CASCADE,
    FOREIGN KEY (subtema_id) REFERENCES subtema(id) ON DELETE CASCADE,
    UNIQUE(prova_secao_id, subtema_id)
);

CREATE INDEX IF NOT EXISTS idx_prova_secao_subtema_secao ON prova_secao_subtema(prova_secao_id);

-- Relacionamento N:M entre Questao e ProvaSecao
CREATE TABLE IF NOT EXISTS questao_prova_secao (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id      INTEGER NOT NULL,
    prova_secao_id  INTEGER NOT NULL,
    numero_questao  INTEGER,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (questao_id) REFERENCES questao(id) ON DELETE CASCADE,
    FOREIGN KEY (prova_secao_id) REFERENCES prova_secao(id) ON DELETE CASCADE,
    UNIQUE(questao_id, prova_secao_id)
);

CREATE INDEX IF NOT EXISTS idx_questao_prova_secao_questao ON questao_prova_secao(questao_id);
CREATE INDEX IF NOT EXISTS idx_questao_prova_secao_secao ON questao_prova_secao(prova_secao_id);

-- ==============================================================================
-- 2. AUTO-MIGRAÇÃO DE DADOS LEGADOS
-- ==============================================================================

-- A) Criar "Prova Única" para todos os concursos existentes
INSERT INTO prova (concurso_id, nome)
SELECT id, 'Prova Única' FROM concurso;

-- B) Criar Seção "Conhecimentos Gerais" para a Prova Única
INSERT INTO prova_secao (prova_id, nome, ordem)
SELECT id, 'Conhecimentos Gerais', 1 FROM prova;

-- C) Vincular todos os cargos do concurso à sua respectiva Prova Única
INSERT INTO prova_cargo (prova_id, concurso_cargo_id)
SELECT p.id, cc.id 
FROM prova p
JOIN concurso_cargo cc ON cc.concurso_id = p.concurso_id;

-- D) Migrar o Edital (Subtemas) da antiga concurso_cargo_subtema para a nova prova_secao_subtema
INSERT INTO prova_secao_subtema (prova_secao_id, subtema_id)
SELECT DISTINCT ps.id, ccs.subtema_id
FROM concurso_cargo_subtema ccs
JOIN prova_cargo pc ON pc.concurso_cargo_id = ccs.concurso_cargo_id
JOIN prova p ON p.id = pc.prova_id
JOIN prova_secao ps ON ps.prova_id = p.id;

-- E) Migrar as Questões para a nova estrutura
INSERT INTO questao_prova_secao (questao_id, prova_secao_id)
SELECT q.id, ps.id
FROM questao q
JOIN prova p ON p.concurso_id = q.concurso_id
JOIN prova_secao ps ON ps.prova_id = p.id
WHERE q.concurso_id IS NOT NULL;

-- F) Atribuir Peso 1.0 para todos nessa seção única
INSERT INTO prova_secao_peso (prova_secao_id, peso)
SELECT id, 1.0 FROM prova_secao;

-- G) Limpeza da tabela legada (Substituída pela nova estrutura de provas)
DROP TABLE IF EXISTS concurso_cargo_subtema;