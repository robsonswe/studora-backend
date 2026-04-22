-- ========================================
-- V11: Add Normalization Columns for Accent-Insensitive Search
-- ========================================

-- DISCIPLINA
ALTER TABLE disciplina ADD COLUMN nome_normalized TEXT;
CREATE INDEX idx_disciplina_nome_norm ON disciplina (nome_normalized);

-- TEMA
ALTER TABLE tema ADD COLUMN nome_normalized TEXT;
CREATE INDEX idx_tema_nome_norm ON tema (nome_normalized);

-- SUBTEMA
ALTER TABLE subtema ADD COLUMN nome_normalized TEXT;
CREATE INDEX idx_subtema_nome_norm ON subtema (nome_normalized);

-- BANCA
ALTER TABLE banca ADD COLUMN nome_normalized TEXT;
CREATE INDEX idx_banca_nome_norm ON banca (nome_normalized);

-- INSTITUICAO
ALTER TABLE instituicao ADD COLUMN nome_normalized TEXT;
ALTER TABLE instituicao ADD COLUMN area_normalized TEXT;
CREATE INDEX idx_instituicao_nome_norm ON instituicao (nome_normalized);
CREATE INDEX idx_instituicao_area_norm ON instituicao (area_normalized);

-- CARGO
ALTER TABLE cargo ADD COLUMN nome_normalized TEXT;
ALTER TABLE cargo ADD COLUMN area_normalized TEXT;
CREATE INDEX idx_cargo_nome_norm ON cargo (nome_normalized);
CREATE INDEX idx_cargo_area_norm ON cargo (area_normalized);
