-- V14__Migrate_To_SecaoCargo.sql

-- 1. BACKUP DAS TABELAS ANTIGAS
ALTER TABLE prova RENAME TO old_prova;
ALTER TABLE prova_secao RENAME TO old_prova_secao;
ALTER TABLE questao_prova_secao RENAME TO old_questao_prova_secao;

-- 2. CRIAÇÃO DAS NOVAS TABELAS (ARQUITETURA CARGO-CENTRIC COM HERANÇA)

CREATE TABLE secao_cargo (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_cargo_id INTEGER NOT NULL REFERENCES concurso_cargo(id) ON DELETE CASCADE,
    nome              TEXT    NOT NULL,
    peso              REAL NOT NULL DEFAULT 1.0,
    nota_minima       REAL,
    ordem             INTEGER,
    num_questoes      INTEGER,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE secao_cargo_subtema (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    secao_cargo_id  INTEGER NOT NULL REFERENCES secao_cargo(id) ON DELETE CASCADE,
    subtema_id      INTEGER NOT NULL REFERENCES subtema(id) ON DELETE CASCADE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(secao_cargo_id, subtema_id)
);

CREATE TABLE prova (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    concurso_id       INTEGER NOT NULL REFERENCES concurso(id) ON DELETE CASCADE,
    concurso_cargo_id INTEGER NOT NULL REFERENCES concurso_cargo(id) ON DELETE CASCADE,
    nome              TEXT    NOT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prova_secao (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    prova_id        INTEGER NOT NULL REFERENCES prova(id) ON DELETE CASCADE,
    secao_cargo_id  INTEGER NOT NULL REFERENCES secao_cargo(id) ON DELETE CASCADE,
    nome            TEXT    NOT NULL, -- Nome local na prova
    ordem           INTEGER NOT NULL,
    num_questoes    INTEGER,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(prova_id, ordem)
);

CREATE TABLE questao_prova_secao (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id      INTEGER NOT NULL REFERENCES questao(id) ON DELETE CASCADE,
    prova_secao_id  INTEGER NOT NULL REFERENCES prova_secao(id) ON DELETE CASCADE,
    numero_questao  INTEGER,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(questao_id, prova_secao_id)
);

-- 3. MIGRAÇÃO E DESMEMBRAMENTO DE DADOS

-- A) Criar as Provas (Uma por cargo que participava da prova antiga)
INSERT INTO prova (concurso_id, concurso_cargo_id, nome, created_at, updated_at)
SELECT op.concurso_id, pc.concurso_cargo_id, op.nome, op.created_at, op.updated_at
FROM old_prova op
JOIN prova_cargo pc ON pc.prova_id = op.id;

-- B) Criar as definições de Seção por Cargo (SecaoCargo)
-- Agrupamos por nome de seção e cargo para consolidar pesos e subtemas
INSERT INTO secao_cargo (concurso_cargo_id, nome, peso, nota_minima)
SELECT DISTINCT 
    p.concurso_cargo_id, 
    ops.nome,
    COALESCE((SELECT psp.peso FROM prova_secao_peso psp WHERE psp.prova_secao_id = ops.id AND (psp.concurso_cargo_id = p.concurso_cargo_id OR psp.concurso_cargo_id IS NULL) ORDER BY psp.concurso_cargo_id DESC LIMIT 1), 1.0),
    (SELECT psp.nota_minima FROM prova_secao_peso psp WHERE psp.prova_secao_id = ops.id AND (psp.concurso_cargo_id = p.concurso_cargo_id OR psp.concurso_cargo_id IS NULL) ORDER BY psp.concurso_cargo_id DESC LIMIT 1)
FROM old_prova_secao ops
JOIN old_prova op ON op.id = ops.prova_id
JOIN prova p ON p.concurso_id = op.concurso_id AND p.nome = op.nome;

-- C) Migrar Subtemas para SecaoCargo
INSERT INTO secao_cargo_subtema (secao_cargo_id, subtema_id)
SELECT sc.id, pss.subtema_id
FROM secao_cargo sc
JOIN prova p ON p.concurso_cargo_id = sc.concurso_cargo_id
JOIN old_prova op ON op.concurso_id = p.concurso_id AND op.nome = p.nome
JOIN old_prova_secao ops ON ops.prova_id = op.id AND ops.nome = sc.nome
JOIN prova_secao_subtema pss ON pss.prova_secao_id = ops.id;

-- D) Criar instâncias de seção nas provas (ProvaSecao)
INSERT INTO prova_secao (prova_id, secao_cargo_id, nome, ordem, num_questoes, created_at, updated_at)
SELECT p.id, sc.id, ops.nome, ops.ordem, ops.num_questoes, ops.created_at, ops.updated_at
FROM old_prova_secao ops
JOIN old_prova op ON op.id = ops.prova_id
JOIN prova p ON p.concurso_id = op.concurso_id AND p.nome = op.nome
JOIN secao_cargo sc ON sc.concurso_cargo_id = p.concurso_cargo_id AND sc.nome = ops.nome;

-- E) Re-vincular as Questões
INSERT INTO questao_prova_secao (questao_id, prova_secao_id, numero_questao, created_at, updated_at)
SELECT oqps.questao_id, ps.id, oqps.numero_questao, oqps.created_at, oqps.updated_at
FROM old_questao_prova_secao oqps
JOIN old_prova_secao ops ON ops.id = oqps.prova_secao_id
JOIN old_prova op ON op.id = ops.prova_id
JOIN prova p ON p.concurso_id = op.concurso_id AND p.nome = op.nome
JOIN secao_cargo sc ON sc.concurso_cargo_id = p.concurso_cargo_id AND sc.nome = ops.nome
JOIN prova_secao ps ON ps.prova_id = p.id AND ps.secao_cargo_id = sc.id;

-- 4. LIMPEZA FINAL
DROP TABLE old_prova;
DROP TABLE old_prova_secao;
DROP TABLE old_questao_prova_secao;
DROP TABLE prova_cargo;
DROP TABLE prova_secao_peso;
DROP TABLE prova_secao_subtema;
