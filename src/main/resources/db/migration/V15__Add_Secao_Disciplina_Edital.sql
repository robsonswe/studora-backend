-- 1. Nova tabela: Disciplina como descrita no Edital
CREATE TABLE secao_disciplina (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    secao_cargo_id  INTEGER NOT NULL REFERENCES secao_cargo(id) ON DELETE CASCADE,
    nome            TEXT NOT NULL,
    ordem           INTEGER NOT NULL DEFAULT 0,
    num_questoes    INTEGER,
    peso            REAL,
    nota_minima     REAL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de ligação: Disciplina do Edital <-> Subtemas (Taxonomia Interna)
CREATE TABLE secao_disciplina_subtema (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    secao_disciplina_id  INTEGER NOT NULL REFERENCES secao_disciplina(id) ON DELETE CASCADE,
    subtema_id           INTEGER NOT NULL REFERENCES subtema(id) ON DELETE CASCADE,
    UNIQUE(secao_disciplina_id, subtema_id)
);

-- 3. Atualizar Questão: Agora a questão sabe a qual "caixinha" do edital pertence
ALTER TABLE questao_prova_secao ADD COLUMN secao_disciplina_id INTEGER REFERENCES secao_disciplina(id);

-- 4. MIGRAÇÃO: Criar disciplina 'Geral' para seções antigas e mover subtemas
INSERT INTO secao_disciplina (secao_cargo_id, nome, ordem)
SELECT id, 'Conteúdo Geral', 1 FROM secao_cargo;

INSERT INTO secao_disciplina_subtema (secao_disciplina_id, subtema_id)
SELECT sd.id, scs.subtema_id FROM secao_cargo_subtema scs
JOIN secao_disciplina sd ON sd.secao_cargo_id = scs.secao_cargo_id;

-- 5. Remover tabela de ligação antiga
DROP TABLE secao_cargo_subtema;

-- 6. MIGRAÇÃO DE QUESTÕES: Vincular questões existentes à disciplina 'Conteúdo Geral' recém criada
UPDATE questao_prova_secao
SET secao_disciplina_id = (
    SELECT sd.id
    FROM secao_disciplina sd
    JOIN prova_secao ps ON ps.secao_cargo_id = sd.secao_cargo_id
    WHERE ps.id = questao_prova_secao.prova_secao_id
    LIMIT 1
);
