-- ============================================================================
-- V6: pgvector Extension and Historical Investigation Embeddings Table
-- ============================================================================

-- 1. Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Create historical_investigation_embeddings table
CREATE TABLE IF NOT EXISTS historical_investigation_embeddings (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL,
    merchant_id VARCHAR(64) NOT NULL,
    source_text TEXT NOT NULL,
    embedding vector(768),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_hist_embeddings_investigation
        FOREIGN KEY (investigation_id)
        REFERENCES investigations(id)
        ON DELETE CASCADE
);

-- 3. Create HNSW index for cosine similarity vector search
CREATE INDEX IF NOT EXISTS idx_hist_embeddings_vector 
    ON historical_investigation_embeddings 
    USING hnsw (embedding vector_cosine_ops);

-- 4. Create B-tree indexes for fast merchant-scoped filtering and lookup
CREATE INDEX IF NOT EXISTS idx_hist_embeddings_merchant 
    ON historical_investigation_embeddings (merchant_id);

CREATE INDEX IF NOT EXISTS idx_hist_embeddings_investigation 
    ON historical_investigation_embeddings (investigation_id);

CREATE INDEX IF NOT EXISTS idx_hist_embeddings_merchant_created 
    ON historical_investigation_embeddings (merchant_id, created_at DESC);
