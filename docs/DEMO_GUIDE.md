# FinSight Demo & Evaluation Walkthrough

This guide provides an end-to-end walkthrough for judges, technical evaluators, and recruiters to demonstrate FinSight's core capabilities in under 5 minutes.

---

## 1. Quick Start Prerequisites

### Single-Command Launch (Backend + React SPA on Port 8080)
From the project root:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Once running, navigate your browser to:
- **Application Dashboard**: `http://localhost:8080/dashboard`
- **Login Credentials**:
  - `admin` / `admin123` (Full Access)
  - `analyst` / `analyst123` (Operational Access)
  - `operator` / `operator123` (Read-Only Access)
- **Interactive Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

---

## 2. The Evaluator Demonstration Scenario

The demo centers around a real-world payment gateway discrepancy scenario:

```
Order ord_1002 (₹1,000.00)
       │
       ▼
Payment pay_1002 (Captured: ₹1,000.00)
       │
       ├─ Gateway Processing Fee: ₹20.00
       └─ GST on Fee (18%): ₹3.60
       │
       ▼
Expected Net Settlement: ₹976.40 (₹1,000.00 - ₹23.60)
       │
Actual Gateway Settlement set_1002: ₹476.40
       │
       ▼
DISCREPANCY DETECTED: Shortfall of ₹500.00
Exception Type: AMOUNT_MISMATCH | Severity: MEDIUM
```

---

## 3. Step-by-Step Walkthrough

### Step 1: Seed the Evaluation Dataset
Populate the database with ledger records for orders, payments, fees, and settlements:
- **UI**: Log in as `admin`, navigate to **Settings** or **Reconciliation**, and click **"Seed Demo Data"**.
- **Or via cURL**:
```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/demo/seed
```

### Step 2: Trigger Deterministic Reconciliation
Execute the Rules A–H audit engine over the ingested records:
- **UI**: Click **"Run Reconciliation"** on the Dashboard or Reconciliation view.
- **Or via cURL**:
```bash
curl -u analyst:analyst123 -X POST http://localhost:8080/api/reconciliation/run
```
- **What to observe**: The engine evaluates all 8 rules. It detects 1 `AMOUNT_MISMATCH` exception for `ord_1002` with an exact delta of **₹500.00**.

### Step 3: Inspect the Exception in the Operations Dashboard
- **UI**: Navigate to **Exceptions** page.
- Select the `AMOUNT_MISMATCH` exception for `ord_1002`.
- Observe the **Financial Breakdown Card**:
  - Expected: ₹976.40
  - Actual: ₹476.40
  - Discrepancy: ₹500.00
  - Severity: `MEDIUM`

### Step 4: Run the Forensic AI Investigation
- **UI**: Click **"Investigate Exception"**.
- **Or via cURL**:
```bash
curl -u analyst:analyst123 -X POST http://localhost:8080/api/investigations/exp_amount_mismatch_ceb8129e
```
- **What happens under the hood**:
  1. **Evidence Graph Constructed**: Assembles nodes for Order, Payment, Fee, and Settlement; computes a deterministic sufficiency score.
  2. **pgvector RAG Retrieval**: Searches the merchant's historical corpus for similar resolved cases.
  3. **Gemini Forensic Reasoning**: Generates structured hypotheses, identifies contradictions, detects missing evidence, and suggests directed requests.
  4. **Financial Amount Validator**: Compares all figures in AI output against DB ground truth. If amounts differ, deterministic fallback engages automatically.

### Step 5: Review AI Findings & Evidence Graph
On the Investigation Details view, observe:
- **Root Cause Hypothesis**: e.g., "Partial Settlement Withholding / Reserve Escrow".
- **Contradiction Reasoning**: Highlights that payment was captured in full, yet settlement has a ₹500 shortfall without any linked refund or fee record.
- **Evidence Sufficiency Score**: Shows sufficiency percentage based on available graph nodes.
- **Directed Evidence Request**: Concrete recommendation to query the gateway for settlement batch adjustments.

### Step 6: Human-in-the-Loop Resolution & Audit Trail
FinSight enforces human oversight before closing financial exceptions:
- **UI**: Click **"Resolve Exception"** (or Approve / Reject / Escalate).
- **Or via cURL**:
```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/investigations/exp_amount_mismatch_ceb8129e/resolve
```
- Navigate to **Audit Logs** to view the tamper-evident audit record documenting the operator (`admin`), timestamp, and resolution details.

---

## 4. Evaluator Verification Checkpoints

| Feature Checked | Where to Verify | Expected Outcome |
| :--- | :--- | :--- |
| **Deterministic Reconciliation** | `POST /api/reconciliation/run` | Exact ₹500.00 delta flagged under `AMOUNT_MISMATCH`. |
| **AI Safety Boundary** | `docs/AI_SAFETY.md` & code | AI is strictly advisory; never directly mutates balances. |
| **Fail-Safe Fallback** | Disable AI via `AI_ENABLED=false` | Investigations still complete using deterministic rule engine. |
| **Multi-Tenant Isolation** | Test with `merchant_b_analyst` | Cannot see or access Merchant A's transactions or exceptions. |
| **Immutable Audit Log** | `/api/audit-logs` | Every action (reconciliation, investigation, resolution) logged with actor and timestamp. |
