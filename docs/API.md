# FinSight REST API Specification

This document details the REST API specification for FinSight, verified against the Spring Boot controller and security implementations.

- **Base URL**: `http://localhost:8080`
- **Interactive Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI v3 JSON**: `http://localhost:8080/v3/api-docs`

---

## 1. Authentication & Role-Based Access Control

All `/api/**` endpoints (except `/api/health`) require HTTP Basic Authentication. FinSight defines three discrete roles:

| Role | Default Username | Default Password | Authorized Capabilities |
| :--- | :--- | :--- | :--- |
| **`OPERATOR`** | `operator` | `operator123` | Read-only access to dashboard, exceptions, audit logs, and investigation reports (`GET /api/**`). |
| **`ANALYST`** | `analyst` | `analyst123` | Operational access: triggers reconciliation runs, initiates AI investigations (`POST /api/reconciliation/**`, `POST /api/investigations/**`). |
| **`ADMIN`** | `admin` | `admin123` | Full access: manual resolution, approval/rejection, escalation, demo data seeding (`POST /api/demo/**`, `POST /api/investigations/*/resolve`). |

### Multi-Tenancy Architecture
- **Tenant Resolution**: The active merchant context is derived strictly from the authenticated user's `AppUser` assignment via `SecurityContextHolder.getContext().getAuthentication()`.
- **Security Boundary**: Tenant identity cannot be spoofed or overridden by request headers. All data access, exception queries, and vector searches are partitioned by the authenticated user's merchant ID.
- **Multi-Tenant Testing Users**:
  - `merchant_b_operator` / `operator123`
  - `merchant_b_analyst` / `analyst123`
  - `merchant_b_admin` / `admin123`

---

## 2. API Endpoints

### 2.1 Reconciliation Engine

#### Run Global Reconciliation Batch
Executes deterministic Rules A–H against unreconciled ledger entries for the authenticated merchant.
- **Method**: `POST`
- **Path**: `/api/reconciliation/run`
- **Required Role**: `ANALYST` or `ADMIN`
- **Headers**:
  - `Idempotency-Key` (optional, String, max 128 chars): Guarantees that concurrent or duplicate requests return the cached original reconciliation run.
- **Success Response** (`200 OK`):
```json
{
  "totalProcessed": 100,
  "matchedCount": 92,
  "exceptionCount": 8,
  "exceptions": [
    {
      "exceptionId": "exp_amount_mismatch_ceb8129e",
      "orderId": "ord_1002",
      "paymentId": "pay_1002",
      "settlementId": "set_1002",
      "exceptionType": "AMOUNT_MISMATCH",
      "severity": "MEDIUM",
      "expectedAmount": 976.40,
      "actualAmount": 476.40,
      "discrepancyAmount": 500.00,
      "description": "Actual settlement ₹476.40 differs from expected ₹976.40"
    }
  ]
}
```

#### Reconcile Single Payment
- **Method**: `POST`
- **Path**: `/api/reconciliation/payment/{paymentId}`
- **Required Role**: `ANALYST` or `ADMIN`

#### Reconcile Single Order
- **Method**: `POST`
- **Path**: `/api/reconciliation/order/{orderId}`
- **Required Role**: `ANALYST` or `ADMIN`

#### Reconcile Single Settlement
- **Method**: `POST`
- **Path**: `/api/reconciliation/settlement/{settlementId}`
- **Required Role**: `ANALYST` or `ADMIN`

#### Get Reconciliation Engine Status
- **Method**: `GET`
- **Path**: `/api/reconciliation/status`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`
- **Success Response** (`200 OK`):
```json
{
  "status": "OPERATIONAL",
  "engine": "Rule-Based Reconciliation Engine"
}
```

---

### 2.2 Financial Exceptions

#### List Financial Exceptions
Retrieves financial discrepancies for the authenticated merchant.
- **Method**: `GET`
- **Path**: `/api/exceptions`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`
- **Query Parameters**:
  - `paged` (optional, boolean, default: `false`): When `false`, returns a JSON array of all exceptions. When `true`, returns a paginated response.
  - `page` (optional, integer, default: 0)
  - `size` (optional, integer, default: 20)
- **Success Response** (`200 OK` when `paged=false`):
```json
[
  {
    "id": 1,
    "exceptionId": "exp_amount_mismatch_ceb8129e",
    "orderId": "ord_1002",
    "paymentId": "pay_1002",
    "settlementId": "set_1002",
    "exceptionType": "AMOUNT_MISMATCH",
    "severity": "MEDIUM",
    "status": "OPEN",
    "expectedAmount": 976.40,
    "actualAmount": 476.40,
    "discrepancyAmount": 500.00,
    "currency": "INR",
    "detectedAt": "2026-09-03T12:00:00Z"
  }
]
```

#### List Paged Financial Exceptions
- **Method**: `GET`
- **Path**: `/api/exceptions/paged`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`
- **Query Parameters**: `page` (default: 0), `size` (default: 20)
- **Success Response** (`200 OK`):
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 8,
  "totalPages": 1
}
```

#### Get Exception Details
- **Method**: `GET`
- **Path**: `/api/exceptions/{exceptionId}`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`

---

### 2.3 Investigations & AI Forensic Reasoning

#### Investigate Exception
Triggers forensic investigation for a specific exception (or returns cached result if already investigated).
- **Method**: `POST`
- **Path**: `/api/investigations/{exceptionId}`
- **Required Role**: `ANALYST` or `ADMIN`
- **Success Response** (`200 OK`):
```json
{
  "exceptionId": "exp_amount_mismatch_ceb8129e",
  "investigationId": "d3b07384-d113-4e08-bf22-f94dd52dcfd4",
  "summary": "Settlement payout is ₹500.00 lower than the expected net amount.",
  "likelyRootCause": "₹500.00 remains unexplained after accounting for refunds, fees, taxes, and adjustments.",
  "confidenceScore": 96.00,
  "recommendedAction": "HUMAN_REVIEW_REQUIRED",
  "actionTaken": "SENT_TO_HUMAN",
  "autoResolved": false,
  "aiUsed": false,
  "analysisSource": "RULE_BASED_FALLBACK",
  "investigatedAt": "2026-09-03T14:30:00Z",
  "evidence": {
    "exception": {
      "exceptionId": "exp_amount_mismatch_ceb8129e",
      "exceptionType": "AMOUNT_MISMATCH",
      "severity": "MEDIUM",
      "expectedAmount": 976.40,
      "actualAmount": 476.40,
      "discrepancyAmount": 500.00
    },
    "order": { "orderId": "ord_1002", "amount": 1000.00, "status": "PAID" },
    "payment": { "paymentId": "pay_1002", "amount": 1000.00, "status": "SUCCESS" },
    "settlement": { "settlementId": "set_1002", "netAmount": 476.40, "status": "SETTLED" },
    "fees": [ { "feeId": "fee_1002", "feeAmount": 20.00, "taxAmount": 3.60 } ],
    "calculatedAmounts": {
      "expectedSettlement": 976.40,
      "actualSettlement": 476.40,
      "discrepancy": 500.00
    },
    "lineage": "ord_1002 -> pay_1002 -> fee_1002 -> set_1002"
  }
}
```

#### Get Investigation Details
- **Method**: `GET`
- **Path**: `/api/investigations/{exceptionId}`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`

#### Batch Investigate All Open Exceptions
- **Method**: `POST`
- **Path**: `/api/investigations/run`
- **Required Role**: `ANALYST` or `ADMIN`
- **Success Response** (`200 OK`):
```json
{
  "totalProcessed": 8,
  "investigationsCreated": 8,
  "alreadyInvestigated": 0,
  "autoResolved": 0,
  "sentToHuman": 8
}
```

#### Human-in-the-Loop Workflow Endpoints
FinSight provides 4 dedicated review transitions for authorized operators:

1. **Resolve Exception**:
   - **Method**: `POST`
   - **Path**: `/api/investigations/{exceptionId}/resolve`
   - **Required Role**: `ADMIN`
   - Marks exception `RESOLVED` and records resolution in audit log.

2. **Approve Exception**:
   - **Method**: `POST`
   - **Path**: `/api/investigations/{exceptionId}/approve`
   - **Required Role**: `ADMIN`
   - Approves AI diagnosis and moves exception to `RESOLVED`.

3. **Reject Exception**:
   - **Method**: `POST`
   - **Path**: `/api/investigations/{exceptionId}/reject`
   - **Required Role**: `ADMIN`
   - Rejects diagnosis and flags exception for supervisor intervention.

4. **Escalate Exception**:
   - **Method**: `POST`
   - **Path**: `/api/investigations/{exceptionId}/escalate`
   - **Required Role**: `ADMIN`
   - Moves exception status to `ESCALATED` for executive investigation.

---

### 2.4 Operations Dashboard & Audit Logs

#### Get Dashboard Statistics
- **Method**: `GET`
- **Path**: `/api/dashboard/stats`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`
- **Success Response** (`200 OK`):
```json
{
  "totalTransactions": 100,
  "reconciledCount": 92,
  "unreconciledCount": 8,
  "matchRatePercentage": 92.0,
  "criticalExceptionsCount": 2,
  "totalDiscrepancyAmount": 14250.00
}
```

#### Get Audit Logs
- **Method**: `GET`
- **Path**: `/api/audit-logs`
- **Required Role**: `OPERATOR`, `ANALYST`, or `ADMIN`
- **Query Parameters**: `page` (default: 0), `size` (default: 20)
- **Success Response** (`200 OK`):
```json
{
  "content": [
    {
      "id": 1,
      "entityType": "INVESTIGATION",
      "entityId": "d3b07384-d113-4e08-bf22-f94dd52dcfd4",
      "merchantId": "merchant_a",
      "action": "AI_INVESTIGATION_SUCCESS",
      "performedBy": "GEMINI_AI_INVESTIGATOR",
      "createdAt": "2026-09-03T14:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 2.5 Demo & Evaluation Utilities

#### Seed Demo Scenario
Seeds the deterministic evaluation scenario (order `ord_1002`, payment `pay_1002`, ₹500 discrepancy).
- **Method**: `POST`
- **Path**: `/api/demo/seed`
- **Required Role**: `ADMIN`
- **Success Response** (`200 OK`):
```json
{
  "status": "SUCCESS",
  "message": "Demo data successfully seeded for merchant_a"
}
```

#### Validate Demo Scenario
- **Method**: `POST`
- **Path**: `/api/demo/validate`
- **Required Role**: `ADMIN`
- Runs the validation workflow programmatically and returns the assertion report.
