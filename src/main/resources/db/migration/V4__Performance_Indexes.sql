-- ========================================
-- V4: Performance Indexes
-- ========================================
-- Purpose: Add missing indexes for optimal query performance
-- Impact: Improves response time for question attempt history and cargo associations

-- Index 1: Composite index for retrieving question attempts chronologically
-- Optimizes: SELECT * FROM resposta WHERE questao_id = ? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_resposta_questao_created 
ON resposta (questao_id, created_at DESC);

-- Index 2: Index for question-cargo foreign key lookups
-- Optimizes: CASCADE DELETE operations and questao-based cargo queries
CREATE INDEX IF NOT EXISTS idx_questao_cargo_questao 
ON questao_cargo (questao_id);
