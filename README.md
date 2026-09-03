# FinSight

**AI-powered financial reconciliation and forensic exception investigation platform.**

---

## 📌 Problem

In modern payment gateway ecosystems (e.g., Razorpay, Stripe, Adyen), financial operations teams manage millions of transactions across fragmented systems: merchant orders, gateway authorization events, processor batches, refunds, interchange fee schedules, and bank disbursements.

Reconciliation at this scale suffers from critical operational bottlenecks:
- **Timing Discrepancies**: Settlement delays (T+1 to T+3) trigger false alarms.
- **Complex Fee Math**: Variable interchange percentages, GST, and chargeback debits create subtle balance shortfalls that spreadsheets cannot reliably detect.
- **Investigation Fatigue**: When discrepancies occur, finance analysts spend hours manually assembling transaction records across disparate logs.
- **Risk of LLM Hallucination**: Generic AI systems cannot be trusted with financial calculations because probabilistic models invent numbers, misstate balances, and fail compliance audits.

---

## 💡 Solution

**FinSight** bridges deterministic financial engineering with safe, forensic AI:

1. **Deterministic Reconciliation First**: An automated engine audits ingested ledgers against 8 mathematical rules (Rules A–H), computing exact ground-truth balance deltas.
2. **Evidence Graph Aggregation**: Assembles connected transaction records (Order → Payment → Fees → Refunds → Settlement) into a structured graph with provenance tracking and deterministic sufficiency scoring.
3. **pgvector Hybrid RAG**: Retrieves historical resolved precedents scoped strictly to the authenticated merchant using cosine similarity, recency decay, and severity weighting.
4. **Advisory Forensic AI**: Google Gemini 1.5 Flash reasons over the verified evidence graph to formulate root-cause hypotheses, detect operational contradictions, and propose directed evidence requests.
5. **Strict Financial Amount Validation**: A defensive backend validator checks all AI text against database ground truth. Any mismatched or hallucinated amount triggers an immediate fallback to a deterministic rule analyzer.
6. **Human-in-the-Loop & Audit Logging**: Finance analysts review findings and authorize resolutions. Every event is captured in an immutable audit trail.

---

## 🏛️ Key Architecture

```
Transaction Data (Orders, Payments, Refunds, Fees, Settlements)
                 │
                 ▼
     Deterministic Reconciliation (Rules A–H)
                 │
                 ▼
        Financial Exceptions
                 │
                 ▼
          Evidence Graph (Nodes, Provenance, Sufficiency Score)
                 │
                 ▼
  Historical Retrieval + Hybrid RAG (pgvector 768-dim, Tenant-Scoped)
                 │
                 ▼
    Gemini Forensic Investigation (Hypotheses, Contradictions, Requests)
                 │
                 ├── Fail-Safe / Amount Mismatch ──► Rule-Based Fallback
                 ▼
          Human Review (Analyst Verification & Resolution)
                 │
                 ▼
           Audit Log (Immutable Database Ledger)
```

---

## ⚡ Core Capabilities

- **Deterministic Reconciliation**: Automated multi-way reconciliation across orders, payments, fees, refunds, adjustments, and settlements.
- **Rules A–H Audit**: Complete coverage of standard payment gateway failure modes:
  - `AMOUNT_MISMATCH` (Rule A)
  - `MISSING_SETTLEMENT` (Rule B)
  - `UNMATCHED_PAYMENT` (Rule C)
  - `DUPLICATE_PAYMENT` (Rule D)
  - `UNMATCHED_REFUND` (Rule E)
  - `FEE_OVERCHARGE` (Rule F)
  - `UNMATCHED_ADJUSTMENT` (Rule G)
  - `CURRENCY_MISMATCH` (Rule H)
- **Financial Evidence Aggregation**: Assembles multi-hop transaction lineages into verified evidence bundles.
- **Evidence Graph**: Explicit node graph tracking entity provenance and availability (`FOUND`, `MISSING`, `NOT_APPLICABLE`, `UNAVAILABLE`).
- **Evidence Sufficiency Scoring**: Deterministic 0–100 score indicating whether enough data exists for diagnosis.
- **Historical Investigation Retrieval**: Vector-based semantic search over previously resolved cases.
- **Hybrid pgvector RAG**: Combined vector cosine similarity, temporal recency decay, and severity matching.
- **Forensic Hypotheses**: Root-cause diagnostic theories ranked by likelihood with supporting evidence references.
- **Contradiction Reasoning**: Explicit identification of conflicting states across payment processor and bank records.
- **Missing Evidence Detection**: Pinpoints absent records required to confirm root causes.
- **Directed Evidence Requests**: Actionable instructions for operators to query specific counterparties (e.g. gateway ARN inquiries).
- **Cross-Exception Correlation & Anomaly Detection**: Identifies shared attributes across discrepancies to detect systemic gateway failures.
- **Strict Multi-Tenant Merchant Isolation**: ThreadLocal `MerchantContext` and unbypassable SQL predicates prevent cross-merchant leakage.
- **Role-Based Access Control (RBAC)**: Method-level security (`OPERATOR`, `ANALYST`, `ADMIN`).
- **AI Safety & Hallucination Defense**: Financial amount validator ensures AI never modifies or misstates monetary values.
- **Human-in-the-Loop Resolution**: Required human authorization before closing exceptions.
- **Immutable Audit Logging**: Tamper-evident logging of all reconciliations, investigations, and resolutions.
- **Concurrency & Idempotency Protection**: Distributed execution locks per merchant and deduplicated exception generation.
- **Incomplete-Data Handling**: Resilient execution when external gateway records are missing or partially synchronized.

---

## 🛡️ AI Safety Principle

> ### **"You investigate money. You do not control money."**

In FinSight:
- **AI is strictly advisory**: The AI engine has zero write authority over financial ledgers, account balances, or settlement payouts.
- **Deterministic ground truth remains authoritative**: Financial balances are calculated by deterministic code from database records.
- **Fail-safe fallback**: If the external AI service times out, is disabled, returns invalid JSON, or fails amount validation, FinSight automatically routes to an internal rule-based diagnostic engine.

For complete safety specifications, see [`docs/AI_SAFETY.md`](docs/AI_SAFETY.md).

---

## 🔍 Retrieval-Augmented Generation (RAG)

FinSight uses a specialized, closed-corpus RAG architecture:
- **Embedding Model**: Google `text-embedding-004` (768 dimensions).
- **768-Dimension HNSW Index**: Fast approximate nearest-neighbor search using cosine distance (`vector_cosine_ops`).
- **Historical Resolved Cases**: Corpus contains only previously audited and resolved investigations.
- **Merchant-Scoped Retrieval**: Strict database-level partition (`WHERE merchant_id = :merchantId`) guarantees zero cross-tenant precedent retrieval.
- **Hybrid Ranking**: Composite scoring: 50% semantic similarity (cosine threshold ≥ 0.50) + 25% exception type match + 15% severity match + 10% amount magnitude closeness.
- **Bounded Retrieval**: Maximum 3 historical precedents injected into context to prevent context dilution.
- **Anti-Prompt-Injection Sanitization**: Strips prompt delimiters, control characters, and markdown injection vectors from retrieved notes.
- **RAG Fallback**: Gracefully degrades to keyword lookup or pure Evidence Graph analysis if the vector extension or embeddings are unavailable.

For full RAG technical details, see [`docs/RAG_ARCHITECTURE.md`](docs/RAG_ARCHITECTURE.md).

---

## 💻 Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.3.3, Spring Security 6.x, Spring Data JPA, Hibernate, HikariCP |
| **Frontend** | React 19, TypeScript, Vite 8, Tailwind CSS v4, shadcn/ui, TanStack Query v5, Lucide Icons |
| **Database & Vector** | PostgreSQL (Supabase / local), pgvector extension (768-dim HNSW indexing), H2 (in-memory test isolation) |
| **AI Layer** | Google Gemini 1.5 Flash API, Deterministic Rule Fallback Engine, Financial Amount Validator |
| **Build & Tooling** | Maven 3.9 (with `frontend-maven-plugin` for unified packaging), Docker, Docker Compose |
| **API Documentation** | OpenAPI 3.0, SpringDoc Swagger UI (`/swagger-ui/index.html`) |

---

## 🧪 Testing & Verification

The FinSight repository is verified with an automated test suite across all architectural layers:

```
Total Automated Tests:   138
Failures:                 0
Errors:                   0
Skipped:                  0
Test Suites (Classes):    25
Build Status:             BUILD SUCCESS
```

### Verification Layer Breakdown:
- **Deterministic Reconciliation (Rules A–H)**: Validates batch execution, edge-case math, and all 8 exception types.
- **Security & Multi-Tenant Isolation**: Verifies RBAC method security and strict cross-tenant data separation.
- **Evidence Graph & Sufficiency**: Tests graph assembly, edge provenance, and deterministic 0–100 scoring.
- **AI Safety & Adversarial Defenses**: 14 tests validating prompt injection defense, malicious payloads, and financial amount validation.
- **pgvector Hybrid RAG**: 22 tests verifying semantic retrieval, cosine similarity, merchant filtering, and fallback behavior.
- **Synthetic Dataset Validation**: 10 tests running the reconciliation engine against large, randomized transaction datasets.

*(Note: AI outputs are probabilistic by nature and are backed by deterministic amount validation and fail-safe rule fallbacks; we do not make unsupported claims of "100% AI accuracy".)*

For complete testing details and suite breakdowns, see [`docs/TESTING.md`](docs/TESTING.md).

---

## 🚀 Running Locally

### Prerequisites
- **Java 21** or newer (`java -version`)
- **Node.js 20+** (Optional: embedded Node 24 is automatically downloaded by Maven during package)

---

### Option A: Unified Single-Port Launch (Recommended)
FinSight packages the React SPA directly into Spring Boot. Both the REST API and the frontend are served from a single port (**8080**):

```bash
# 1. Clone the repository
git clone https://github.com/sohammayekar1406-afk/FinSight.git
cd FinSight

# 2. Configure environment (optional - defaults to safe local placeholders)
cp .env.example .env

# 3. Build the unified package (compiles React, bundles static assets, runs all 138 tests)
# Windows:
.\mvnw.cmd clean package

# Linux / macOS:
./mvnw clean package

# 4. Run the application
# Windows:
.\mvnw.cmd spring-boot:run

# Linux / macOS:
./mvnw spring-boot:run
```

#### Access Endpoints:
| Interface | URL |
| :--- | :--- |
| **Operations Dashboard (React SPA)** | `http://localhost:8080/dashboard` |
| **Login Page** | `http://localhost:8080/login` |
| **Swagger UI / OpenAPI** | `http://localhost:8080/swagger-ui/index.html` |
| **Health Endpoint** | `http://localhost:8080/api/health` |

#### Default Credentials:
- **Administrator**: `admin` / `admin123`
- **Financial Analyst**: `analyst` / `analyst123`
- **Operations Operator**: `operator` / `operator123`

---

### Option B: Separate Frontend Development Server
If you are developing frontend components with hot-module replacement (Vite HMR):

```bash
# Terminal 1: Backend
.\mvnw.cmd spring-boot:run

# Terminal 2: Frontend
cd next-app
npm install
npm run dev
```
Frontend development server opens at `http://localhost:5173` (proxies `/api` to `http://localhost:8080`).

---

### Option C: Docker Deployment
```bash
# Build and run with Docker Compose
docker-compose up --build
```

---

## 📁 Project Structure

```
FinSight/
├── README.md                           # Primary platform documentation
├── pom.xml                             # Maven parent configuration & frontend plugin
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── .mvn/                               # Maven wrapper configuration
├── .gitignore                          # Git exclusions (build, logs, credentials)
├── .env.example                        # Environment variables template
├── Dockerfile                          # Multi-stage production container build
├── docker-compose.yml                  # Container deployment specification
│
├── src/                                # Backend source code
│   ├── main/
│   │   ├── java/com/ledgerlens/
│   │   │   ├── FinSightApplication.java # Spring Boot main application class
│   │   │   ├── config/                 # Security, CORS, AI, & DB configuration
│   │   │   ├── controller/             # REST API controllers
│   │   │   ├── dto/                    # Data transfer objects & schemas
│   │   │   ├── entity/                 # JPA entity domain model
│   │   │   ├── exception/              # Global error handling
│   │   │   ├── repository/             # Spring Data JPA repositories
│   │   │   ├── security/               # RBAC filters & user services
│   │   │   └── service/                # Reconciliation, Evidence Graph, RAG, & AI services
│   │   │       ├── ai/                 # Gemini API client & FinancialAmountValidator
│   │   │       ├── analyzer/           # Rule-based fallback analyzer
│   │   │       └── rag/                # pgvector semantic retrieval & embeddings
│   │   └── resources/
│   │       ├── application.yml         # Application configuration & env mappings
│   │       ├── db/migration/           # Flyway / pgvector SQL migrations
│   │       └── static/                 # Production React assets (built by Maven)
│   │
│   └── test/java/com/ledgerlens/       # Automated test suite (25 classes, 138 tests)
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
