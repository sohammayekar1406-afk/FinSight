# FinSight Hybrid RAG Architecture

FinSight integrates a domain-specific Retrieval-Augmented Generation (RAG) pipeline designed specifically for financial exception investigations. It combines dense vector semantic search over historical resolved cases with structured deterministic filtering and hybrid reranking.

---

## 1. RAG Design Philosophy

In financial investigations, generic web-search RAG or unbounded document retrieval creates noise and hallucination risks. FinSight applies RAG with four strict constraints:

1. **Deterministic Ground Truth First**:
   The current exception's Evidence Graph is the primary source of truth. RAG provides *historical precedents*, not current ledger facts.
2. **Strict Multi-Tenant Scoping**:
   Investigations from Merchant A can never be retrieved to assist an investigation for Merchant B.
3. **Closed Corpus**:
   The vector corpus contains only previously resolved, audited investigations within the merchant's own organization.
4. **Bounded Retrieval**:
   A maximum of 3 highly relevant historical cases are retrieved to avoid context dilution and prompt injection.

---

## 2. pgvector Storage & Indexing Architecture

Historical investigations are embedded and stored in PostgreSQL using the `pgvector` extension.

### Database Schema

```sql
CREATE TABLE historical_investigation_embeddings (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations(id) ON DELETE CASCADE,
    merchant_id VARCHAR(64) NOT NULL,
    source_text TEXT NOT NULL,
    embedding vector(768),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- HNSW Cosine Distance Index for fast approximate nearest-neighbor search
CREATE INDEX idx_hist_embeddings_vector 
    ON historical_investigation_embeddings 
    USING hnsw (embedding vector_cosine_ops);

-- B-tree indexes for multi-tenant isolation and temporal ordering
CREATE INDEX idx_hist_embeddings_merchant 
    ON historical_investigation_embeddings (merchant_id);

CREATE INDEX idx_hist_embeddings_merchant_created 
    ON historical_investigation_embeddings (merchant_id, created_at DESC);
```

### Vector Specifications
- **Embedding Dimension**: 768 dimensions (compatible with Google Vertex/Gemini embedding models).
- **Index Type**: Hierarchical Navigable Small World (HNSW) graph for sub-millisecond retrieval latency.
- **Metric**: Cosine similarity (`vector_cosine_ops`).

---

## 3. The Retrieval Pipeline

```
           New Financial Exception Detected
                           │
                           ▼
             Generate Query Text Representation
           (Exception Type, Discrepancy, Evidence)
                           │
                           ▼
              Generate 768-dim Embedding Vector
                           │
                           ▼
          Execute Merchant-Scoped Vector Search
         SELECT * FROM historical_investigation_embeddings
         WHERE merchant_id = :merchantId
         ORDER BY embedding <=> :queryVector
         LIMIT 10
                           │
                           ▼
                 Hybrid Ranking Engine
       Combined Score = 0.50 * Semantic Similarity (Cosine)
                      + 0.25 * Exception Type Match
                      + 0.15 * Severity Match
                      + 0.10 * Amount Magnitude Closeness
                           │
                           ▼
               Select Top-K Precedents (K ≤ 3, Cosine ≥ 0.50)
                           │
                           ▼
             Sanitize & Format Context Block
                           │
                           ▼
          Inject into Gemini Investigation Prompt
```

---

## 4. Hybrid Ranking Algorithm & Verification

Pure vector cosine similarity can match cases with similar terminology but entirely different financial dynamics. FinSight's `HybridHistoricalCaseRanker` scores candidate cases using a composite function combining dense embeddings with deterministic ledger attributes:

$$\text{Final Score} = (w_{\text{sem}} \times S_{\text{semantic}}) + (w_{\text{type}} \times S_{\text{type}}) + (w_{\text{sev}} \times S_{\text{severity}}) + (w_{\text{amt}} \times S_{\text{amount}})$$

Where:
- $w_{\text{sem}} = 0.50$ (`DEFAULT_SEMANTIC_WEIGHT`): Cosine similarity between query embedding and historical case vector (pre-filtered with minimum threshold $S_{\text{semantic}} \ge 0.50$).
- $w_{\text{type}} = 0.25$ (`DEFAULT_EXCEPTION_TYPE_WEIGHT`): Exact exception type match (1.0 for identical type, 0.0 otherwise).
- $w_{\text{sev}} = 0.15$ (`DEFAULT_SEVERITY_WEIGHT`): Severity level match (1.0 for identical severity, 0.0 otherwise).
- $w_{\text{amt}} = 0.10$ (`DEFAULT_AMOUNT_WEIGHT`): Monetary magnitude closeness: $\frac{\min(|A_1|, |A_2|)}{\max(|A_1|, |A_2|)}$, measuring discrepancy scale alignment.
- $\text{Max Results}$: Configurable limit, defaulting to 3 cases (`ragMaxResults`).

---

## 5. Security & Isolation Guarantees

### Tenant Partitioning
The vector query enforces an unbypassable SQL parameter:
```sql
WHERE merchant_id = :merchantId
```
Even if a vector in Merchant B's space has a 0.99 cosine similarity to an exception in Merchant A's space, the database index filter guarantees it is never returned.

### Prompt Injection Defense on Historical Context
Historical case notes written by external counterparties could contain adversarial text. FinSight sanitizes retrieved historical notes:
- Replaces prompt boundary markers (`---`, ````json`, `###`).
- Enforces strict character and token limits per historical case.
- Injects precedents within an explicit `<historical_precedents>` XML boundary that instructs the LLM to treat them purely as background reference.

---

## 6. Fallback Behavior

FinSight's RAG layer is fail-safe:
- **No pgvector Extension / Local H2 Test Mode**: The system falls back to structured keyword and exception-type retrieval without error.
- **Zero Historical Cases Available**: The investigation executes smoothly using the Evidence Graph and current ledger data alone.
- **Embedding API Failure**: The system proceeds with investigation analysis without historical context, preserving system availability.
