# FinSight Testing Methodology & Verification Report

FinSight features a comprehensive automated test suite designed to validate financial accuracy, deterministic reconciliation rules, multi-tenant isolation, AI forensic reasoning, and pgvector RAG safety.

---

## 1. Executive Test Summary

| Metric | Result |
| :--- | :--- |
| **Total Automated Tests** | **154** |
| **Failures** | **0** |
| **Errors** | **0** |
| **Skipped** | **0** |
| **Build Status** | **BUILD SUCCESS** |

> **Note on Accuracy**: The 100% pass rate represents the automated regression suite verifying deterministic reconciliation, mathematical invariant checks, security boundaries, and fallback behavior. AI forensic reasoning is probabilistic by nature and guarded by the Financial Amount Validator and deterministic fallback engine.

---

## 2. Test Architecture & Distinct Layers

Financial systems require strict separation between deterministic mathematical rules and probabilistic AI reasoning. FinSight reflects this separation in its testing architecture:

```
┌─────────────────────────────────────────────────────────────┐
│             Layer 1: Deterministic Ledger Audits            │
│         Mathematical rules, balance deltas, Rules A-H       │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│           Layer 2: Reliability, Security & Concurrency      │
│     Merchant isolation, RBAC, distributed execution locks   │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│             Layer 3: Evidence Graph & Sufficiency           │
│   Node relationships, availability, deterministic scoring   │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│        Layer 4: AI Forensic Reasoning & Safety Bounds       │
│  Amount validator, prompt injection defense, fail-safe rules│
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│           Layer 5: pgvector Hybrid RAG & Embeddings         │
│  Cosine similarity, HNSW indexing, multi-tenant vector split│
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│        Layer 6: Lifecycle Regression & Demo Validation      │
│  Reconciliation ≠ auto-investigate, seeding idempotency     │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Test Suite Breakdown (154 Tests)

### Layer 1: Core Deterministic Reconciliation Engine (Rules A–H)
- **`ReconciliationServiceTest`** (4 tests):
  Validates batch reconciliation, state transitions, rule evaluations, and exception generation.
- **`RuleBasedInvestigationAnalyzerTest`** (9 tests):
  Validates deterministic fallback diagnoses for all 8 exception types (`AMOUNT_MISMATCH`, `MISSING_SETTLEMENT`, `UNMATCHED_PAYMENT`, `DUPLICATE_PAYMENT`, `UNMATCHED_REFUND`, `FEE_OVERCHARGE`, `UNMATCHED_ADJUSTMENT`, `CURRENCY_MISMATCH`).
- **`DemoValidationTest`** (5 tests):
  Verifies the end-to-end demo scenario (order `ord_1002`, payment `pay_1002`, ₹500 discrepancy).
- **`ExceptionSeverityServiceTest`** (4 tests):
  Tests deterministic severity assignment based on transaction value and impact.

### Layer 2: Security, RBAC & Multi-Tenant Isolation
- **`SecurityAuthorizationTest`** (5 tests):
  Tests Spring Security `@EnableMethodSecurity` role enforcement (`OPERATOR`, `ANALYST`, `ADMIN`).
- **`MerchantIsolationIntegrationTest`** (8 tests):
  Guarantees that Merchant A cannot view, query, or resolve Merchant B's transactions or exceptions.
- **`Phase3SecurityTest`** (4 tests):
  Validates authentication filter, token validation, and unauthorized access rejections.
- **`ReconciliationConcurrencyIntegrationTest`** (1 test):
  Validates atomic merchant execution locks (`reconciliation_execution_lock`) to prevent concurrent race conditions.
- **`IdempotencyAndPaginationTest`** (3 tests):
  Tests pagination boundaries and idempotent execution behavior.

### Layer 3: Evidence Graph & Sufficiency Scoring
- **`Phase3_5EvidenceGraphTest`** (10 tests):
  Validates graph construction, node status assignment (`FOUND`, `MISSING`, `NOT_APPLICABLE`, `UNAVAILABLE`), edge provenance, and deterministic 0–100 sufficiency scoring.

### Layer 4: AI Forensic Reasoning & Financial Safety Bounds
- **`AiInvestigationAnalyzerTest`** (5 tests):
  Validates structured JSON prompt generation, Gemini API integration, and schema parsing.
- **`Phase3AiIntegrationTest`** (6 tests):
  Tests end-to-end AI investigation pipeline with mock response validation.
- **`Phase3ForensicReasoningTest`** (15 tests):
  Tests hypothesis generation, contradiction identification, missing evidence detection, and directed evidence requests.
- **`Phase3AnomalyDetectionTest`** (3 tests):
  Tests pattern analysis across multiple transactions for soft operational anomalies.
- **`Phase3HistoricalInvestigationTest`** (5 tests):
  Tests historical precedent lookup and resolution pattern matching.
- **`Phase4AdversarialAiTest`** (11 tests):
  Tests defense against adversarial prompt injection, currency manipulation, unauthorized instruction overrides, and hallucinated financial figures.
- **`InvestigationServiceTest`** (6 tests):
  Tests investigation service orchestration, state persistence, and human review resolution.
- **`InvestigationIntegrationTest`** (1 test):
  End-to-end integration test of investigation lifecycle.

### Layer 5: Synthetic Dataset Validation
- **`Phase5SyntheticDatasetValidationTest`** (19 tests):
  Validates the reconciliation engine against large, randomized synthetic datasets representing real-world edge cases (partial refunds, delayed settlements, split fees).

### Layer 6: pgvector Hybrid RAG & Vector Safety
- **`Phase6RagEvaluationTest`** (5 tests):
  Evaluates semantic retrieval accuracy, cosine similarity ordering, and hybrid reranking (semantic + recency + severity).
- **`Phase6MerchantIsolationRagTest`** (3 tests):
  Verifies that pgvector queries strictly enforce `WHERE merchant_id = ?`, preventing cross-tenant vector leakage.
- **`Phase6AdversarialRagSafetyTest`** (3 tests):
  Tests prompt injection resistance on retrieved historical case notes.
- **`Phase6RagFallbackTest`** (4 tests):
  Tests graceful degradation when vector database or embeddings are empty or unavailable.

### Layer 7: Lifecycle Regression & Demo Validation
- **`ReconciliationDoesNotInvestigateTest`** (3 tests):
  Verifies that running reconciliation never automatically invokes AI investigation — exceptions are created as `OPEN`, never auto-investigated.
- **`CleanSeedReproductionTest`** and **`InvestigationAppearanceRegressionTest`**:
  Lifecycle regression: clean → seed → reconcile → 4 deterministic exceptions → explicit investigate → 0 auto-investigations.
- **`DashboardServiceTest`** (4 tests):
  Tests aggregation of KPI metrics, match rates, and active discrepancy totals.
- **`GlobalExceptionHandlerTest`** (3 tests):
  Tests standardized error response formatting for bad requests, missing entities, and security violations.

---

## 4. Running the Test Suite Locally

### Run All Backend Tests
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Run a Specific Test Class
```bash
# Example: Run the pgvector RAG evaluation tests
.\mvnw.cmd test -Dtest=Phase6RagEvaluationTest

# Example: Run AI safety & adversarial tests
.\mvnw.cmd test -Dtest=Phase4AdversarialAiTest

# Example: Verify reconciliation lifecycle separation
.\mvnw.cmd test -Dtest=ReconciliationDoesNotInvestigateTest
```

### Skip Frontend During Backend Tests
```bash
.\mvnw.cmd test "-Dskip.frontend=true"
```

### Frontend Typecheck & Build Verification
```bash
cd next-app
npm run typecheck
npm run build
```
