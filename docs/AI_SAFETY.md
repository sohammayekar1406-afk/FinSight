# FinSight AI Safety & Forensic Reasoning Framework

FinSight is built upon a fundamental architectural commitment:

> **"You investigate money. You do not control money."**

In financial systems, LLMs must never be given write authority over ledger balances, settlement payouts, or reconciliation rules. This document details the defensive engineering layers that keep FinSight safe, verifiable, and free from financial hallucinations.

---

## 1. The Financial Advisory Boundary

1. **AI is strictly read-only**: The AI model analyzes financial evidence and produces structured hypotheses. It has zero capability to mutate ledger records, execute database writes to financial balances, or trigger bank transfers.
2. **Deterministic Ground Truth is Authoritative**: Mathematical calculations (net settlement, fee sums, discrepancy deltas) are computed deterministically in Java before the AI is invoked. The AI is evaluated against the ground truth, not the reverse.
3. **Mandatory Human-in-the-Loop**: All AI suggestions must be reviewed by a human financial analyst or admin before an exception can be formally resolved or closed.

---

## 2. Financial Amount Validation & Hallucination Prevention

A common failure mode of LLMs in financial applications is numeric drift—inventing or modifying monetary figures in natural language output. FinSight implements a **Financial Amount Validator** (`FinancialAmountValidator`):

```
       Gemini Output (JSON)
                │
                ▼
  FinancialAmountValidator.validate()
                │
    ┌───────────┴───────────┐
    │ Extract all monetary  │
    │ values from AI text   │
    └───────────┬───────────┘
                │
    ┌───────────▼───────────┐
    │ Compare against DB    │
    │ ground truth set:     │
    │ {order, payment, fee, │
    │  refund, settlement,  │
    │  discrepancy amounts} │
    └───────────┬───────────┘
                │
         Does AI contain
      unauthorized amounts?
         /             \
       YES             NO
       /                 \
      ▼                   ▼
  TRIGGER FALLBACK     ACCEPT AI
 (Discard AI response, (Persist forensic
  use deterministic     analysis & hypotheses)
  rule analyzer)
```

If the LLM introduces any unexpected numeric amount not present in the verified Evidence Graph, the entire AI response is marked invalid and FinSight immediately falls back to the deterministic rule analyzer.

---

## 3. Forensic Reasoning Structure

Rather than generating open-ended chat text, the AI engine is restricted by schema to 4 structured forensic components:

### 1. Root Cause Hypotheses
Each hypothesis provides:
- **Title**: Brief diagnostic summary (e.g., "Unrecorded Partial Refund").
- **Likelihood**: `HIGH`, `MEDIUM`, or `LOW`.
- **Reasoning**: Specific evidentiary justification grounded in node attributes.
- **Supporting Evidence**: Explicit node IDs referenced from the Evidence Graph.

### 2. Contradiction Reasoning
Identifies conflicts across ledger entries (e.g., "Payment record marked SUCCESS on gateway, but bank settlement batch shows zero corresponding credit").

### 3. Missing Evidence Identification
Categorizes data gaps that prevent definitive diagnosis:
- Distinguishes between expected records missing from the database vs. records that do not apply to this transaction lifecycle.

### 4. Directed Evidence Requests
Provides actionable, concrete instructions for human operators (e.g., "Request gateway settlement batch report for ARN 88392104 from merchant bank").

---

## 4. Adversarial AI & Prompt Injection Defenses

Financial notes, transaction descriptions, and external merchant payloads can contain adversarial instructions designed to hijack LLM behavior. FinSight defends against this through:

1. **Input Sanitization**:
   All user-controlled strings (customer notes, merchant descriptions, external reference IDs) are sanitized before prompt assembly. Control characters, prompt-delimiter sequences (e.g., `---`, `### System:`, `Ignore previous instructions`), and markdown injection vectors are neutralized.
2. **Schema-Enforced JSON Output**:
   The model is instructed to output strictly valid JSON conforming to the `AiInvestigationResponse` schema. Markdown wrappers and conversational filler are stripped during parsing.
3. **Context Length Clamping**:
   Input prompts and historical RAG contexts have hard token ceilings to prevent prompt exhaustion attacks.

---

## 5. Fail-Closed Fallback Architecture

FinSight operates reliably even when external AI services are completely unavailable:

| Condition | System Response |
| :--- | :--- |
| **No API Key configured** | Graceful fallback to `RuleBasedInvestigationAnalyzer` (zero startup failure). |
| **Network Timeout (> 5000ms)** | Timeout caught; deterministic rule-based analysis persisted. |
| **Invalid JSON / Syntax Error** | JSON parse exception caught; deterministic rule-based analysis returned. |
| **Hallucinated Amount Detected** | Validation failure logged; deterministic rule-based analysis returned. |
| **Rate Limit (429) / Gateway (502)** | Handled fail-safe without throwing uncaught 500 errors to the client. |

In all fallback scenarios, the investigation is completed with high diagnostic quality based on deterministic ledger audits, ensuring operational continuity.

---

## 6. Audit Trail & Human Accountability

- Every AI investigation is logged in the `investigations` and `audit_logs` tables with:
  - Exact model name and version used (e.g., `gemini-1.5-flash` or `rule-based-fallback`).
  - Analysis timestamp.
  - Full input evidence snapshot.
  - Diagnostic output.
- When an analyst resolves an exception:
  - The resolving user's identity is recorded.
  - Action taken (`MANUAL_ADJUSTMENT`, `REFUND_ISSUED`, `MERCHANT_CREDIT`, `DISMISS`) is persisted.
  - Resolution notes are permanently stored for compliance auditing.
