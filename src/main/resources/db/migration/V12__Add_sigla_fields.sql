-- Add sigla fields to banca and instituicao tables

-- Add sigla and sigla_normalized columns to banca table
ALTER TABLE banca ADD COLUMN sigla VARCHAR(50);
ALTER TABLE banca ADD COLUMN sigla_normalized VARCHAR(50);

-- Add index on sigla_normalized for performance
CREATE INDEX idx_banca_sigla_norm ON banca(sigla_normalized);

-- Add sigla and sigla_normalized columns to instituicao table
ALTER TABLE instituicao ADD COLUMN sigla VARCHAR(50);
ALTER TABLE instituicao ADD COLUMN sigla_normalized VARCHAR(50);

-- Add index on sigla_normalized for performance
CREATE INDEX idx_instituicao_sigla_norm ON instituicao(sigla_normalized);