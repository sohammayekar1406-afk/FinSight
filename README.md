# FinSight

**AI Finance Controller — deterministic financial reconciliation with forensic AI exception investigation.**

---
## 🚀 Live Demo

**TRY FINSIGHT LIVE:https://finsight-production-f61a.up.railway.app/**

> Demo credentials are provided in the application for evaluation.

## 🚀 Quick Start — One Command, One URL

> **The only command you need:**
>
> ```bash
> # Windows
> .\mvnw.cmd spring-boot:run
>
> # Linux / macOS
> ./mvnw spring-boot:run
> ```
>
> **Then open: [`http://localhost:8080`](http://localhost:8080)**
>
> Login with `admin` / `admin123` (or `analyst` / `analyst123`, `operator` / `operator123`).
>
> FinSight is a **single-server app**. Spring Boot serves both the REST API and the React frontend on port 8080. There is no separate frontend server. **Do not use port 5173 to evaluate the full application** — that port is a Vite HMR dev server for frontend-only development (see [Option B](#option-b--frontend-hmr-dev-mode-frontend-only-requires-backend-at-8080) below).

---

## 📌 Problem

In modern payment gateway ecosystems (Razorpay, Stripe, Adyen), finance-operations teams manage millions of transactions across fragmented systems: merchant orders, gateway authorization events, processor batches, refunds, interchange fee schedules, and bank disbursements.

Reconciliation at this scale suffers from critical operational bottlenecks:

- **Timing Discrepancies**: Settlement delays (T+1 to T+3) trigger false alarms.
- **Complex Fee Math**: Variable interchange percentages, GST, and chargeback debits create subtle balance shortfalls that spreadsheets cannot reliably detect.
- **Investigation Fatigue**: When discrepancies surface, analysts spend hours manually assembling records across disparate logs.
- **LLM Hallucination Risk**: Generic AI systems cannot be trusted with financial calculations — probabilistic models invent numbers, misstate balances, and fail compliance audits.

---

## 💡 Solution

**FinSight** bridges deterministic financial engineering with safe, forensic AI:

```
Raw Financial Data (Orders, Payments, Refunds, Fees, Adjustments, Settlements)
    │
    ▼
Deterministic Reconciliation (Rules A–H)
    │
    ▼
Financial Exceptions
    │
    ▼
Evidence Graph (Provenance + Sufficiency Score)
    │
    ▼
Hybrid RAG (pgvector 768-dim, Merchant-Scoped)
    │
    ▼
Gemini Forensic Investigation (Hypotheses, Contradictions, Directed Requests)
    │
    ├── Hallucination / Timeout / Amount Mismatch ──► Rule-Based Deterministic Fallback
    ▼
Human Review (Analyst Verification & Authorize Resolution)
    │
    ▼
Audit Log (Immutable Database Ledger)
```

> ### **"Deterministic reconciliation is the financial source of truth. AI is an advisory investigation layer and never executes financial actions autonomously."**

---

## ⚡ Key Capabilities

### Deterministic Reconciliation (Rules A–H)
- **Rule A — `AMOUNT_MISMATCH`**: `Expected Settlement = Payment − Refunds − Fees ± Adjustments`. Flags exact delta when actual settlement differs.
- **Rule B — `MISSING_SETTLEMENT`**: Success payment older than threshold with no linked settlement.
- **Rule C — `UNMATCHED_PAYMENT`**: Gateway payment batch record has no matching internal order.
- **Rule D — `DUPLICATE_PAYMENT`**: Multiple successful payments reference the same order.
- **Rule E — `UNMATCHED_REFUND`**: Refund record exists with no corresponding successful payment.
- **Rule F — `FEE_OVERCHARGE`**: Assessed fee exceeds contractual rate schedule.
- **Rule G — `UNMATCHED_ADJUSTMENT`**: Manual adjustment lacks a valid audit authorization reference.
- **Rule H — `CURRENCY_MISMATCH`**: Order, payment, or settlement records carry differing ISO currency codes.

### Evidence Graph
- Multi-hop transaction lineage graph: `Order → Payment → Fees → Refunds → Settlement`
- Node availability status: `FOUND`, `MISSING`, `NOT_APPLICABLE`, `UNAVAILABLE`
- Deterministic sufficiency scoring (0–100) — computed without LLM involvement
- Identifies missing evidence before AI is invoked

### AI Safety Architecture
- **Advisory-only model**: AI has zero write authority over financial ledgers or settlement payouts
- **Financial amount validator**: Compares every figure in AI output against database ground truth; hallucinated amounts trigger immediate deterministic fallback
- **Fail-safe fallback**: Timeout, invalid JSON, disabled AI, or amount mismatch all route to internal rule-based diagnostic engine
- **Prompt-injection sanitization**: Strips prompt delimiters, control characters, and markdown injection vectors from all RAG-retrieved content

### Hybrid Historical RAG (pgvector)
- **Embedding model**: Google `text-embedding-004` (768 dimensions, HNSW cosine index)
- **Merchant-scoped retrieval**: `WHERE merchant_id = :merchantId` enforced at database level
- **Hybrid ranking**: 50% semantic cosine similarity + 25% exception type match + 15% severity match + 10% amount magnitude proximity
- **Cosine threshold**: 0.50 minimum similarity before case is injected into context
- **Bounded context**: Maximum 3 historical precedents to prevent context dilution
- **Graceful degradation**: Falls back to keyword lookup or pure Evidence Graph analysis if vector extension or embeddings are unavailable

### Forensic Investigation (Gemini)
- Competing root-cause hypotheses ranked by likelihood
- Explicit contradiction detection across payment processor and bank records
- Missing-evidence reasoning — pinpoints which records are absent to confirm root cause
- Directed evidence requests — actionable operator instructions (e.g., specific ARN gateway queries)
- Cross-exception correlation for systemic gateway failure patterns

### Security & Isolation
- **Multi-tenant merchant isolation**: ThreadLocal `MerchantContext` + mandatory `merchant_id` SQL predicates — no cross-merchant leakage at repository, security filter, or vector retrieval layers
- **RBAC**: Method-level `@PreAuthorize` for `OPERATOR`, `ANALYST`, `ADMIN` roles
- **Concurrency protection**: Pessimistic distributed row lock per merchant (`reconciliation_execution_lock`) prevents concurrent race conditions
- **Exception deduplication**: SHA-256 composite key prevents stacked exceptions across repeated reconciliation runs
- **Immutable audit trail**: Every reconciliation run, investigation, and resolution captured with actor, timestamp, and metadata
- **Secrets via environment variables**: No credentials committed to version control

---

## 🏛️ Architecture

```
┌──────────────────────────────────────────────────────────┐
│                 React / Vite (SPA)                       │
│     Dashboard · Exceptions · Evidence Graph · Audit      │
└────────────────────────────┬─────────────────────────────┘
                             │  HTTP / REST  (port 8080)
┌────────────────────────────▼─────────────────────────────┐
│               Spring Boot 3 (Embedded Tomcat)            │
│   REST API · Security · RBAC · Merchant Context          │
└────────────────────────────┬─────────────────────────────┘
                             │
             ┌───────────────┼───────────────┐
             ▼               ▼               ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│ Reconciliation │  │  Evidence Graph│  │  RAG / Gemini  │
│  Engine (A–H)  │  │  + Sufficiency │  │  + Fallback    │
└────────┬───────┘  └────────┬───────┘  └────────┬───────┘
         │                   │                   │
┌────────▼───────────────────▼───────────────────▼───────┐
│      PostgreSQL (Supabase) + pgvector (768-dim HNSW)   │
│    Financial ledger · Exception store · Audit log      │
│    Historical investigation embeddings (tenant-scoped) │
└────────────────────────────────────────────────────────┘
                             │
             ┌───────────────▼───────────────┐
             │     Google Gemini 1.5 Flash   │
             │  (text-embedding-004 + Flash) │
             └───────────────────────────────┘
```

### Evidence Graph → RAG Relationship

1. `EvidenceCollectionService` assembles an Evidence Graph from database-verified records for the specific exception.
2. `EvidenceGraphService` computes a deterministic sufficiency score (0–100) without AI involvement.
3. Only after the Evidence Graph is complete does `SemanticHistoricalRetrievalService` retrieve up to 3 merchant-scoped precedents from pgvector.
4. The Evidence Graph data + RAG precedents form the bounded, structured context passed to `GeminiAiInvestigationAnalyzer`.
5. `FinancialAmountValidator` cross-checks all monetary figures in the AI response against the database; any mismatch triggers `RuleBasedInvestigationAnalyzer` as the authoritative fallback.

---

## 💻 Technology Stack

| Domain | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.3.3, Spring Security 6.x, Spring Data JPA, Hibernate, HikariCP |
| **Frontend** | React 19, TypeScript, Vite 8, Tailwind CSS v4, shadcn/ui, TanStack Query v5, Lucide Icons |
| **Database & Vector** | PostgreSQL (Supabase), pgvector extension (768-dim HNSW cosine index), H2 (in-memory test isolation) |
| **AI Layer** | Google Gemini 1.5 Flash API (investigation), `text-embedding-004` (RAG embeddings), Deterministic Rule Fallback Engine, Financial Amount Validator |
| **Build & Tooling** | Maven 3.9 with `frontend-maven-plugin` (unified packaging), Docker, Docker Compose |
| **API Documentation** | OpenAPI 3.0, SpringDoc Swagger UI (`/swagger-ui/index.html`) |

---

## 🔄 Application Workflow

The full lifecycle is a deliberate, explicit sequence:

```
1. Clean Slate          POST /api/demo/reset
        │
        ▼
2. Seed Data            POST /api/demo/seed
   (Creates: Orders, Payments, Fees, Refunds, Settlements)
   (Does NOT create: Exceptions, Investigations, Embeddings)
        │
        ▼
3. Run Reconciliation   POST /api/reconciliation/run
   (Deterministic Rules A–H audit)
   (Creates: Financial Exceptions with status=OPEN)
   (Does NOT invoke AI investigation automatically)
        │
        ▼
4. Financial Exceptions appear in Operations Dashboard
   (Click any exception to view detail)
        │
        ▼
5. Explicit Diagnose    POST /api/investigations/{exceptionId}
   (User must explicitly click "Investigate Exception")
   (Builds Evidence Graph → RAG retrieval → Gemini analysis)
        │
        ▼
6. AI Investigation Report
   (Hypotheses · Contradictions · Evidence Requests · Sufficiency Score)
        │
        ▼
7. Human Review         POST /api/investigations/{exceptionId}/resolve
   (Analyst reviews findings, authorizes resolution)
        │
        ▼
8. Audit Log            GET /api/audit-logs
   (Immutable record: actor, action, timestamp, metadata)
```

> **Important**: Seeding data does not create investigations. Reconciliation does not automatically invoke AI. Investigations require an explicit human-initiated `POST /api/investigations/{exceptionId}`.

---

## 🛡️ AI Safety

> ### **"You investigate money. You do not control money."**

FinSight enforces a hard architectural boundary between investigation and execution:

- **AI is strictly advisory**: The AI engine generates forensic hypotheses and evidence requests. It cannot alter ledger balances, approve payouts, or bypass deterministic reconciliation rules.
- **Deterministic ground truth is authoritative**: All financial amounts are computed by deterministic backend code from database records, never by an LLM.
- **Amount validation is non-negotiable**: `FinancialAmountValidator` compares every monetary figure in AI-generated text against the database. If any figure mismatches by any amount, the entire AI response is discarded and `RuleBasedInvestigationAnalyzer` produces the authoritative diagnosis.
- **Fail-closed reliability**: Any AI service failure (timeout, invalid JSON, disabled via config, network error) automatically activates the deterministic fallback — no operational disruption.
- **Prompt-injection sanitization**: Retrieved RAG notes are sanitized before context injection to prevent jailbreak attempts via historical case content.

For complete safety specifications, see [`docs/AI_SAFETY.md`](docs/AI_SAFETY.md).

---

## 🔍 RAG Architecture

FinSight uses a specialized, closed-corpus, merchant-scoped RAG architecture:

| Component | Specification |
| :--- | :--- |
| **Embedding Model** | Google `text-embedding-004` (768 dimensions) |
| **Vector Index** | HNSW with `vector_cosine_ops` (fast approximate nearest-neighbor) |
| **Corpus** | Only previously audited and resolved investigations for the authenticated merchant |
| **Tenant Isolation** | `WHERE merchant_id = :merchantId` enforced at SQL layer — zero cross-tenant leakage |
| **Hybrid Ranking** | 50% cosine similarity + 25% exception type match + 15% severity match + 10% amount magnitude |
| **Similarity Threshold** | Minimum 0.50 cosine score before case is injected into context |
| **Bounded Retrieval** | Maximum 3 historical precedents per investigation |
| **Fallback** | Keyword lookup or pure Evidence Graph analysis if pgvector or embeddings are unavailable |

For full RAG technical details, see [`docs/RAG_ARCHITECTURE.md`](docs/RAG_ARCHITECTURE.md).

---

## 🧪 Testing & Verification

Verified automated test suite across all architectural layers:

```
Total Automated Tests:    154
Failures:                   0
Errors:                     0
Skipped:                    0
Build Status:         BUILD SUCCESS
```

### Verification Layer Breakdown

- **Deterministic Reconciliation (Rules A–H)**: Batch execution, edge-case math, all 8 exception types.
- **Security & Multi-Tenant Isolation**: RBAC method security, strict cross-tenant data separation, concurrent execution locks.
- **Evidence Graph & Sufficiency**: Graph assembly, edge provenance, deterministic 0–100 scoring.
- **AI Safety & Adversarial Defenses**: 14 tests validating prompt injection defense, malicious payloads, and financial amount validation.
- **pgvector Hybrid RAG**: Semantic retrieval, cosine similarity, merchant filtering, fallback behavior.
- **Synthetic Dataset Validation**: Reconciliation engine against large, randomized transaction datasets (partial refunds, delayed settlements, split fees).
- **Lifecycle Regression**: Verifies reconciliation does not auto-invoke investigation; investigations require explicit user action.

> AI outputs are probabilistic and guarded by deterministic amount validation and fail-safe rule fallback; no unsupported claims of "100% AI accuracy" are made.

For complete testing details and suite breakdowns, see [`docs/TESTING.md`](docs/TESTING.md).

---

## 🚀 Running Locally

### Prerequisites
- **Java 21** or newer (`java -version`)
- **Node.js 20+** *(optional — Maven auto-downloads Node 24 during package if not present)*

---

### Option A — Single-Server Launch *(Standard)*

FinSight packages the React SPA directly into Spring Boot — both API and frontend are served from **one port, one URL**.

**Step 1 — Clone and configure**
```bash
git clone https://github.com/sohammayekar1406-afk/FinSight.git
cd FinSight

# Copy the environment template
cp .env.example .env
# Edit .env: fill in your Supabase/PostgreSQL credentials and Gemini API key
```

**Step 2 — Build frontend into Spring Boot** *(required once; repeat on frontend source changes)*
```bash
# Windows
.\mvnw.cmd clean package -DskipTests

# Linux / macOS
./mvnw clean package -DskipTests
```

**Step 3 — Run**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

**Open: [`http://localhost:8080`](http://localhost:8080)**

| Interface | URL |
| :--- | :--- |
| **Operations Dashboard** | `http://localhost:8080/dashboard` |
| **Login Page** | `http://localhost:8080/login` |
| **Swagger UI / OpenAPI** | `http://localhost:8080/swagger-ui/index.html` |
| **Health Endpoint** | `http://localhost:8080/api/health` |

Default evaluation credentials:

| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin123` | Full access (reconciliation, investigation, resolution) |
| `analyst` | `analyst123` | Operational access (investigate, approve, escalate) |
| `operator` | `operator123` | Read-only (view exceptions, dashboard, audit logs) |
| `merchant_b_admin` | `admin123` | Isolated tenant — cross-tenant isolation verification |

---

### Option B — Frontend HMR Dev Mode *(frontend-only — requires backend at :8080)*

> [!WARNING]
> **`http://localhost:5173` is NOT the full application.** It is a Vite hot-reload server that proxies `/api` to `http://localhost:8080`. If Spring Boot is not running at `:8080`, every API call returns **502 Bad Gateway**.
>
> Use this mode **only** when actively developing React components and needing instant hot-module replacement.

```bash
# Terminal 1 — start the backend first (required)
.\mvnw.cmd spring-boot:run

# Terminal 2 — start the Vite HMR dev server
cd next-app
npm install
npm run dev:frontend-only
```

---

### Option C — Docker Deployment

```bash
docker-compose up --build
```

> **Production testing note**: The production database is persistent. Run `POST /api/demo/reset` after any manual testing to restore clean state before a live demo.

---

## 📁 Project Structure

```
FinSight/
├── README.md                           # Primary platform documentation
├── pom.xml                             # Maven parent configuration & frontend plugin
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── .mvn/                               # Maven wrapper configuration
├── .gitignore                          # Git exclusions (build, logs, credentials)
├── .env.example                        # Environment variables template (no real secrets)
├── Dockerfile                          # Multi-stage production container build
├── docker-compose.yml                  # Container deployment specification
│
├── src/                                # Backend source code
│   ├── main/
│   │   ├── java/com/ledgerlens/
│   │   │   ├── FinSightApplication.java # Spring Boot main application class
│   │   │   ├── config/                 # Security, CORS, AI, & DB configuration
│   │   │   ├── controller/             # REST API controllers
│   │   │   ├── dto/                    # Data transfer objects & API schemas
│   │   │   ├── entity/                 # JPA entity domain model
│   │   │   ├── exception/              # Global error handling
│   │   │   ├── repository/             # Spring Data JPA repositories
│   │   │   ├── security/               # RBAC filters & user services
│   │   │   └── service/                # Reconciliation, Evidence Graph, RAG & AI services
│   │   │       ├── ai/                 # Gemini API client & FinancialAmountValidator
│   │   │       ├── analyzer/           # Rule-based fallback analyzer
│   │   │       └── rag/                # pgvector semantic retrieval & embeddings
│   │   └── resources/
│   │       ├── application.yml         # Application configuration & env mappings
│   │       ├── db/migration/           # Flyway / pgvector SQL migrations
│   │       └── static/                 # Production React assets (built by Maven)
│   │
│   └── test/java/com/ledgerlens/       # Automated test suite (154 tests)
│
├── next-app/                           # Frontend React SPA
│   ├── package.json                    # Frontend dependencies & scripts
│   ├── vite.config.ts                  # Vite configuration & API proxy
│   ├── tsconfig.json                   # TypeScript configuration
│   └── src/
│       ├── api/                        # Axios API clients
│       ├── components/                 # UI components (Dashboard, Evidence Graph, Review)
│       ├── hooks/                      # TanStack Query custom hooks
│       ├── layouts/                    # App navigation & top bar layouts
│       ├── pages/                      # Dashboard, Exceptions, Reconciliation views
│       └── types/                      # TypeScript interface contracts
│
└── docs/                               # Detailed technical documentation
    ├── ARCHITECTURE.md                 # System architecture & ledger state machine
    ├── AI_SAFETY.md                    # Forensic reasoning bounds & amount validation
    ├── RAG_ARCHITECTURE.md             # pgvector schema, HNSW indexing & hybrid ranking
    ├── API.md                          # Complete REST API specification
    ├── TESTING.md                      # Testing methodology & test suite breakdown
    └── DEMO_GUIDE.md                   # Step-by-step evaluator walkthrough
```

---

## 🎯 Demo Walkthrough for Evaluators

```
Step 1: POST /api/demo/reset         — Restore clean slate (0 transactions, 0 exceptions)
Step 2: POST /api/demo/seed          — Seed 83 orders, 84 payments, 9 fees, 5 settlements
Step 3: POST /api/reconciliation/run — Run deterministic Rules A–H audit
         ↓ 4 deterministic exceptions created:
         • CURRENCY_MISMATCH  (pay_hero_5)        ₹0.00 discrepancy
         • UNEXPECTED_FEE     (fee_hero_2)         ₹62.00 discrepancy
         • UNEXPECTED_FEE     (fee_hero_4)         ₹393.73 discrepancy
         • AMOUNT_MISMATCH    (set_1002)           ₹500.00 discrepancy
Step 4: Open any exception in the Exceptions page
Step 5: Click "Investigate Exception" — Evidence Graph + RAG + Gemini analysis
Step 6: Inspect Evidence Graph, Hypotheses, Contradictions, Directed Requests
Step 7: Click "Resolve" / "Approve" — Human-in-the-loop review
Step 8: Navigate to Audit Logs — Immutable record of all actions
```

---

## 🏗️ Deployment Architecture

Production runs as a unified deployment:

```
Railway (PaaS)
    └── Spring Boot JAR
         ├── REST API  (/api/*)
         └── React SPA (/* → index.html)
                │
         Supabase PostgreSQL
              ├── Financial ledger tables
              ├── pgvector extension (768-dim HNSW)
              └── Historical investigation embeddings
                │
         Google Gemini API
              ├── gemini-1.5-flash  (forensic investigation)
              └── text-embedding-004 (RAG embeddings)
```

**Production URL**: `https://finsight-production-f61a.up.railway.app`

The production frontend is built with `npm run build` in `next-app/`, then synchronized into `src/main/resources/static/` during Maven packaging — serving both API and SPA from a single port (8080) with no separate frontend server or CDN required.

---

## 🔑 Environment Variables

Configure via `.env` (local) or Railway environment settings (production). See [`.env.example`](.env.example) for all variable names.

| Variable | Description |
| :--- | :--- |
| `SUPABASE_DB_URL` | PostgreSQL JDBC URL |
| `SUPABASE_DB_USERNAME` | Database username |
| `SUPABASE_DB_PASSWORD` | Database password |
| `AI_ENABLED` | `true` to enable Gemini AI (`false` → deterministic fallback only) |
| `AI_API_KEY` | Google Gemini API key |
| `AI_MODEL` | AI model name (default: `gemini-1.5-flash`) |
| `AI_TIMEOUT_MS` | AI request timeout in milliseconds |
| `SECURITY_ADMIN_USERNAME` | Admin credential username |
| `SECURITY_ADMIN_PASSWORD` | Admin credential password |
| `SECURITY_ANALYST_USERNAME` | Analyst credential username |
| `SECURITY_ANALYST_PASSWORD` | Analyst credential password |
| `SECURITY_OPERATOR_USERNAME` | Operator credential username |
| `SECURITY_OPERATOR_PASSWORD` | Operator credential password |
| `FRONTEND_ORIGIN` | Allowed CORS origins (comma-separated) |

> **Never commit real credentials.** All sensitive values must be supplied through environment variables or a secrets manager.

---

## 📖 In-Depth Documentation

- 🏛️ [System Architecture](docs/ARCHITECTURE.md)
- 🛡️ [AI Safety Framework & Forensic Reasoning](docs/AI_SAFETY.md)
- 🔍 [pgvector Hybrid RAG Design](docs/RAG_ARCHITECTURE.md)
- 📡 [REST API Specification](docs/API.md)
- 🧪 [Testing Methodology & Results](docs/TESTING.md)
- 🎯 [Demo & Evaluation Walkthrough](docs/DEMO_GUIDE.md)

---

## ⚖️ License & Disclaimers

FinSight is built as an enterprise demonstration and competition submission for financial reconciliation and forensic operations. Built with Spring Boot, React, and Google Gemini.
