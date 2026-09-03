# FinSight System Architecture & Engineering Design

FinSight is an enterprise financial operations platform that performs deterministic reconciliation, exception detection, and AI-assisted forensic investigation across payment gateways, merchant ledgers, and bank settlements.

---

## 1. End-to-End System Architecture

```
                    ┌─────────────────────────────────────────┐
                    │       Transaction Ingestion Layer       │
                    │   Orders, Payments, Refunds, Fees,      │
                    │         Settlements, Adjustments        │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │   Deterministic Reconciliation Engine   │
                    │          Rules A through H Audits       │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │      Financial Exception Detection      │
                    │  AMOUNT_MISMATCH, MISSING_SETTLEMENT,   │
                    │  UNMATCHED_PAYMENT, DUPLICATE_PAYMENT,  │
                    │  FEE_OVERCHARGE, CURRENCY_MISMATCH...   │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │         Evidence Graph Layer            │
                    │  Provenance Tracking, Availability,     │
                    │  Deterministic Sufficiency Scoring      │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │   Historical Retrieval & Hybrid RAG     │
                    │    pgvector (768-dim HNSW Cosine),      │
                    │    Merchant Isolation, Recency Weight   │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │      AI Forensic Reasoning Engine       │
                    │   Gemini 1.5 Flash (Bounded Schema),    │
                    │   Hypotheses, Contradictions, Requests  │
                    └────────────────────┬────────────────────┘
                                         │
                                         ├─── Hallucination / Timeout / Error
                                         │    └──► Rule-Based Deterministic Fallback
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │   Amount Validation & Integrity Check   │
                    │  Strict DB Ground Truth Cross-Check     │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │     Human-in-the-Loop Review Queue      │
                    │   Analyst Verification, Approve/Reject  │
                    └────────────────────┬────────────────────┘
                                         │
                                         ▼
                    ┌─────────────────────────────────────────┐
                    │       Immutable Audit Trail (DB)        │
                    │   Actor, Action, Timestamp, Metadata    │
                    └─────────────────────────────────────────┘
```

---

## 2. Core Architecture Principles

1. **Deterministic Ledger Ground Truth**:
   The relational database is the sole source of financial truth. Amounts, lineage connections, and mathematical balance calculations are computed deterministically by backend services, never by an LLM.

2. **Advisory AI Boundary ("Investigate, Not Control")**:
   Artificial intelligence is restricted to an investigative role. The AI generates forensic hypotheses, identifies potential operational contradictions, and formulates evidence requests. It cannot alter account balances, approve payouts, or bypass deterministic rules.

3. **Strict Multi-Tenant Isolation**:
   Every database query, background job, and vector search operates under a mandatory `merchant_id` predicate. Cross-tenant data leakage is prevented at the repository, security filter, and RAG retrieval layers.

4. **Fail-Closed Reliability**:
   If the external AI service times out, returns malformed JSON, fails financial amount validation, or is disabled via configuration, the system automatically falls back to an internal rule-based deterministic diagnostic analyzer without operational disruption.

---

## 3. Ingestion & Ledger State Machine

Financial entities follow an immutable audit ledger lifecycle:
- **Order**: Initiated with gross order amount, currency, merchant ID, and status (`CREATED`, `PAID`, `CANCELLED`).
- **Payment**: Payment gateway authorization with gateway reference, captured amount, and status (`SUCCESS`, `FAILED`, `PENDING`).
- **Refund**: Associated with an original payment, specifying gross refund amount and reason.
- **Gateway Fee**: Assessed processing fees and applicable taxes (e.g. GST) per transaction.
- **Adjustment**: Manual adjustments, chargeback debits/credits, or promotional concessions.
- **Settlement**: Actual batch disbursement from the acquirer/gateway to the merchant bank account.

---

## 4. Deterministic Reconciliation Engine (Rules A–H)

Reconciliation executes as an atomic batch process for a specific merchant or across all merchants:

| Rule | Exception Code | Mathematical Condition / Trigger |
| :--- | :--- | :--- |
| **Rule A** | `AMOUNT_MISMATCH` | $\text{Expected Settlement} = \text{Payment Amount} - \text{Refunds} - \text{Fees} \pm \text{Adjustments}$. If $\text{Actual Settlement} \neq \text{Expected Settlement}$, an exception is flagged with the exact delta. |
| **Rule B** | `MISSING_SETTLEMENT` | Payment status is `SUCCESS`, age exceeds configured delay threshold (default 24h), and no linked settlement record exists. |
| **Rule C** | `UNMATCHED_PAYMENT` | Payment record exists in the gateway batch but no matching merchant order exists in the internal ledger. |
| **Rule D** | `DUPLICATE_PAYMENT` | Multiple successful payment records reference the same unique order identifier. |
| **Rule E** | `UNMATCHED_REFUND` | Refund record exists with no corresponding successful payment record. |
| **Rule F** | `FEE_OVERCHARGE` | Assessed gateway fee exceeds the agreed contractual rate schedule. |
| **Rule G** | `UNMATCHED_ADJUSTMENT` | Manual adjustment or dispute debit lacks a valid audit authorization reference. |
| **Rule H** | `CURRENCY_MISMATCH` | Order, payment, or settlement records have differing ISO currency codes. |

---

## 5. Evidence Graph Architecture

To eliminate hallucination and provide structured context to both human analysts and the AI engine, FinSight constructs an explicit **Evidence Graph**:

### Node Entities
- `ORDER`, `PAYMENT`, `REFUND`, `FEE`, `ADJUSTMENT`, `SETTLEMENT`, `EXCEPTION`

### Availability Statuses
- **`FOUND`**: Record exists in the database and was verified.
- **`MISSING`**: Record is expected given the transaction lineage but absent from the database.
- **`NOT_APPLICABLE`**: Record type is irrelevant for this specific exception category.
- **`UNAVAILABLE`**: Record cannot be retrieved due to external upstream gateway partition.

### Deterministic Sufficiency Scoring (0–100)
Sufficiency is calculated deterministically in `EvidenceGraphService` without LLM involvement:
1. **Required Entities per Exception Type**:
   - `AMOUNT_MISMATCH`: Requires `ORDER`, `PAYMENT`, `SETTLEMENT`
   - `MISSING_SETTLEMENT`: Requires `PAYMENT`, `SETTLEMENT`
   - `MISSING_PAYMENT`: Requires `ORDER`, `PAYMENT`
   - `UNEXPECTED_FEE`: Requires `PAYMENT`, `FEE`, `SETTLEMENT`
   - `DISCREPANT_REFUND`: Requires `PAYMENT`, `REFUND`
   - `UNMATCHED_ADJUSTMENT`: Requires `PAYMENT`, `SETTLEMENT`, `ADJUSTMENT`
   - `CURRENCY_MISMATCH`: Requires `PAYMENT`, `ORDER`
   - `DUPLICATE_TRANSACTION`: Requires `PAYMENT`
2. **Formula**:
   $$\text{Sufficiency Score} = \left(\frac{\text{Required Entities Found}}{\text{Total Required Entities for Exception Type}}\right) \times 100$$
3. **Assessment Thresholds**:
   - **`SUFFICIENT`** ($\text{Score} \ge 80.00$): "All or most critical evidence is available for forensic analysis"
   - **`PARTIAL`** ($50.00 \le \text{Score} < 80.00$): "Some evidence is missing; analysis may be limited or inconclusive"
   - **`INSUFFICIENT`** ($\text{Score} < 50.00$): "Critical evidence is missing; high-confidence analysis not possible"

---

## 6. Multi-Tenant Isolation Architecture

FinSight enforces strict data separation for multi-merchant payment platforms:
1. **Security Context Derivation**: `MerchantContext` retrieves the active `merchant_id` directly from the authenticated principal via `SecurityContextHolder.getContext().getAuthentication()` mapped through `AppUser`. Tenant identity is bound to credentials and cannot be spoofed via HTTP headers.
2. **Repository Predicates**: Every Spring Data JPA query explicitly scopes by `merchantId` (e.g. `findByExceptionIdAndMerchantId`, `findByMerchantId`).
3. **Cross-Tenant Prevention**: If an analyst attempts to access an exception or investigation belonging to another merchant, a 404/403 security boundary terminates the request.
4. **Vector Partitioning**: RAG embeddings in pgvector are strictly filtered with `WHERE merchant_id = :merchantId`.

---

## 7. Concurrency & Idempotency

- **Pessimistic Distributed Row Lock**: The `reconciliation_execution_lock` table manages atomic execution per merchant. When reconciliation runs with an `Idempotency-Key` header, `reconciliationExecutionLockRepository.findByIdForUpdate("MERCHANT:" + merchantId)` serializes concurrent execution.
- **Idempotency Caching**: Supplying an identical `Idempotency-Key` (up to 128 characters) returns the cached `ReconciliationRun` payload without re-executing ledger rules.
- **Exception Deduplication**: Running reconciliation multiple times over identical ledger entries detects existing exceptions by natural composite keys, preventing duplicate records.

---

## 8. Unified Deployment Architecture

FinSight packages both the Spring Boot backend and the React SPA into a single deployable artifact:
1. `frontend-maven-plugin` executes `npm run build` during Maven packaging.
2. Vite outputs static chunks to `next-app/dist/`.
3. `maven-resources-plugin` places these assets into `target/classes/static/`.
4. Spring Boot's embedded Tomcat serves both REST APIs (`/api/*`) and the frontend SPA (`/*`) on a single port (**8080**), eliminating CORS complexities and reverse proxy requirements in local environments.
