// ============================================================
// FINSIGHT API TYPES
// Auto-derived from backend DTOs and entity enums
// DO NOT invent new fields — verified against actual Java source
// ============================================================

// ---- Enums ----

export type ExceptionType =
  | "AMOUNT_MISMATCH"
  | "MISSING_PAYMENT"
  | "MISSING_SETTLEMENT"
  | "UNEXPECTED_FEE"
  | "DUPLICATE_TRANSACTION"
  | "DELAYED_SETTLEMENT"
  | "DISCREPANT_REFUND"
  | "UNKNOWN_TRANSACTION"
  | "UNMATCHED_ADJUSTMENT"
  | "CURRENCY_MISMATCH"

export type ExceptionSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

export type ExceptionStatus =
  | "OPEN"
  | "INVESTIGATING"
  | "RESOLVED_AUTO"
  | "RESOLVED_MANUAL"
  | "ESCALATED"

export type RecommendedAction =
  | "AUTO_RESOLVE"
  | "HUMAN_REVIEW_REQUIRED"
  | "RETRY_SETTLEMENT"
  | "MANUAL_ADJUSTMENT"

export type ActionTaken =
  | "AUTO_RESOLVED"
  | "SENT_TO_HUMAN"
  | "MANUALLY_OVERRIDDEN"
  | "APPROVED"
  | "REJECTED"
  | "ESCALATED"

// ---- Shared ----

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

// ---- Health ----

export interface HealthResponse {
  status: string
  databaseStatus: string
  reconciliationEngineStatus: string
  aiProviderStatus: string
  timestamp: string
}

// ---- Dashboard ----

export interface DashboardStats {
  totalTransactions: number
  successfulPayments: number
  refundsCount: number
  totalRefundsAmount: number
  totalFeesAmount: number
  totalSettlements: number
  totalSettlementsAmount: number
  unreconciledAmount: number
  openExceptionsCount: number
  severityBreakdown: Record<string, number>
  settlementOverview: Record<string, number>
  recentExceptions: FinancialException[]
}

// ---- Financial Exceptions ----

export interface FinancialException {
  id: string
  exceptionId: string
  merchantId: string
  exceptionType: ExceptionType
  severity: ExceptionSeverity
  status: ExceptionStatus
  expectedAmount: number | null
  actualAmount: number | null
  discrepancyAmount: number | null
  orderId: string | null
  paymentId: string | null
  refundId: string | null
  settlementId: string | null
  description: string
  detectedAt: string
  createdAt: string
  resolvedAt: string | null
}

// ---- Investigation ----

export interface InvestigationEvidence {
  exception?: {
    exceptionId: string
    exceptionType: ExceptionType
    severity: ExceptionSeverity
    status: ExceptionStatus
    discrepancyAmount: number
  }
  lineage?: {
    order?: { orderId: string; amount: number; status: string } | null
    payment?: { paymentId: string; amount: number; status: string } | null
    refunds?: Array<{ refundId: string; amount: number; status: string }>
    fees?: Array<{ feeAmount: number; taxAmount: number; totalFee: number }>
    adjustments?: Array<{ adjustmentId: string; amount: number; type: string }>
    expectedSettlement?: { expectedGross: number; expectedNet: number } | null
    actualSettlement?: {
      settlementId: string
      actualSettledAmount: number
      utr: string
    } | null
    discrepancy?: number
  }
  calculatedAmounts?: Record<string, number>
  [key: string]: unknown
}

export interface EvidenceNode {
  entityType: string
  entityId: string
  availability: string
  relationshipToException: string
  source: string
  amount?: number
  currency?: string
  status?: string
  timestamp?: string
  relevanceReason?: string
}

export interface EvidenceGraph {
  nodes: EvidenceNode[]
  totalNodes: number
  foundNodes: number
  missingNodes: number
  transactionFlow?: string
}

export interface EvidenceSufficiency {
  sufficiencyScore: number
  sufficiencyLevel?: string
  assessment?: string
  isSufficient?: boolean
  criticalEvidenceMissing?: boolean
  missingEvidenceTypes?: string[]
  reasoning?: string
  foundEvidence?: string[]
  missingEvidence?: string[]
}

export interface RagHistoricalCase {
  investigationId: string
  exceptionId: string
  exceptionType: string
  severity: string
  discrepancyAmount: number
  previousRootCause: string
  previousResolution: string
  similarityScore: number
  hybridRankScore: number
}

export interface ForensicHypothesis {
  hypothesis: string
  confidence: number
  supportingEvidence: string[]
  contradictingEvidence: string[]
  status: string
}

export interface ForensicContradiction {
  contradiction: string
  evidenceA: string
  evidenceB: string
  severity: string
  resolution: string
  unresolved: boolean
}

export interface Investigation {
  exceptionId: string
  investigationId: string
  summary: string
  likelyRootCause: string
  confidenceScore: number
  recommendedAction: RecommendedAction
  actionTaken: ActionTaken
  autoResolved: boolean
  aiUsed: boolean
  analysisSource: string
  investigatedAt: string
  evidence: InvestigationEvidence | null
  evidenceGraph?: EvidenceGraph | null
  evidenceSufficiency?: EvidenceSufficiency | null
  ragHistoricalCases?: RagHistoricalCase[]
  hypotheses?: ForensicHypothesis[]
  contradictions?: ForensicContradiction[]
}

export interface RunInvestigationsResult {
  exceptionsProcessed: number
  investigationsCreated: number
  alreadyInvestigated: number
  autoResolved: number
  sentToHuman: number
}

// ---- Reconciliation ----

export interface ReconciliationItemResult {
  itemId: string
  itemType: string
  ruleApplied: string
  passed: boolean
  exceptionCreated: boolean
  message: string
}

export interface ReconciliationResult {
  reconciliationId: string
  recordsChecked: number
  exceptionsCreated: number
  exceptionsAlreadyExisting: number
  successfulChecks: number
  failedChecks: number
  totalDiscrepancyAmount: number
  startedAt: string
  completedAt: string
  items: ReconciliationItemResult[]
}

// ---- Audit Logs ----

export interface AuditLog {
  id: string
  entityType: string
  entityId: string
  action: string
  performedBy: string
  details: string | null
  createdAt: string
}

// ---- Demo ----

export interface SeedResponse {
  message: string
  ordersCreated: string[]
  paymentsCreated: string[]
  settlementsCreated: string[]
}

export interface DemoValidationReport {
  overallStatus: string
  generatedAt: string
  seedSummary: Record<string, unknown>
  reconciliationSummary: Record<string, unknown>
  exceptionsSummary: Record<string, unknown>
  investigationsSummary: Record<string, unknown>
  auditSummary: Record<string, unknown>
  stepsVerified: Array<{
    step: number
    name: string
    status: string
    details: string
  }>
}

// ---- Auth ----

export type UserRole = "OPERATOR" | "ANALYST" | "ADMIN"

export interface AuthCredentials {
  username: string
  password: string
  role: UserRole
}
