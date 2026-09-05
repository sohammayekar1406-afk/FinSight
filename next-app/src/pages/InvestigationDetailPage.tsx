import { useParams, useNavigate } from "react-router-dom"
import { useInvestigationDetail, useRunInvestigation, useResolveException } from "@/hooks/useInvestigations"
import { useExceptionDetail } from "@/hooks/useExceptions"
import { useAuth } from "@/contexts/AuthContext"
import {
  SectionCard,
  SeverityBadge,
  StatusBadge,
  ExceptionTypeBadge,
  LoadingState,
  TransactionLineage,
  formatCurrency,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { toast } from "sonner"
import {
  ArrowLeft,
  Bot,
  CheckCircle2,
  RefreshCw,
  Sparkles,
  Layers,
  History,
  Lightbulb,
  AlertCircle,
} from "lucide-react"

export default function InvestigationDetailPage() {
  const { exceptionId } = useParams<{ exceptionId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()

  const { data: investigation, isLoading: isInvLoading, refetch: refetchInv } = useInvestigationDetail(exceptionId)
  const { data: exception, isLoading: isExpLoading, refetch: refetchExp } = useExceptionDetail(exceptionId)

  const runInvestigation = useRunInvestigation()
  const resolveException = useResolveException()

  const handleRunInvestigation = async () => {
    if (!exceptionId) return
    toast.promise(runInvestigation.mutateAsync(exceptionId), {
      loading: "Gathering lineage ground truth & querying Gemini AI...",
      success: () => {
        refetchInv()
        refetchExp()
        return "AI Investigation report successfully generated."
      },
      error: "Failed to run AI investigation.",
    })
  }

  const handleResolve = async () => {
    if (!exceptionId) return
    toast.promise(resolveException.mutateAsync(exceptionId), {
      loading: "Applying manual resolution override...",
      success: () => {
        refetchInv()
        refetchExp()
        return "Exception manually resolved by ADMIN."
      },
      error: "Failed to resolve exception.",
    })
  }

  if (isInvLoading || isExpLoading) return <LoadingState message="Loading AI investigation report & evidence breakdown..." />

  const lineageNodes = [
    { id: "ord", label: `Order ${exception?.orderId || "—"}`, value: exception?.expectedAmount ? formatCurrency(exception.expectedAmount) : undefined, status: "normal" as const },
    { id: "pay", label: `Payment ${exception?.paymentId || "—"}`, value: exception?.actualAmount ? formatCurrency(exception.actualAmount) : undefined, status: "normal" as const },
    { id: "set", label: `Settlement ${exception?.settlementId || "—"}`, value: exception?.actualAmount ? formatCurrency(exception.actualAmount) : undefined, status: "normal" as const },
    { id: "exp", label: `${exception?.exceptionType || "EXCEPTION"}`, value: `Shortfall ${formatCurrency(exception?.discrepancyAmount)}`, status: exception?.status.startsWith("RESOLVED") ? ("resolved" as const) : ("exception" as const) },
  ]

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top navigation */}
      <div className="flex items-center justify-between">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate("/investigations")}
          className="text-xs text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="w-3.5 h-3.5 mr-1" />
          Back to Investigations
        </Button>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              refetchInv()
              refetchExp()
            }}
            className="text-xs border-border"
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Refresh
          </Button>

          {user?.role !== "OPERATOR" && (
            <Button
              size="sm"
              onClick={handleRunInvestigation}
              disabled={runInvestigation.isPending}
              className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
            >
              <Bot className="w-3.5 h-3.5 mr-1.5" />
              {investigation ? "Re-run Investigation" : "Run AI Investigation"}
            </Button>
          )}

          {user?.role === "ADMIN" && exception?.status !== "RESOLVED_MANUAL" && (
            <Button
              size="sm"
              variant="outline"
              onClick={handleResolve}
              disabled={resolveException.isPending}
              className="text-xs border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/10"
            >
              <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
              Resolve Exception
            </Button>
          )}
        </div>
      </div>

      {/* Header Info */}
      <div className="p-6 rounded-xl border border-border bg-card flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 flex-wrap mb-1">
            <span className="font-mono text-lg font-bold text-foreground">{exceptionId}</span>
            {exception && <ExceptionTypeBadge type={exception.exceptionType} />}
            {exception && <SeverityBadge severity={exception.severity} />}
            {exception && <StatusBadge status={exception.status} />}
          </div>
          <p className="text-xs text-muted-foreground">
            AI Investigation Report & Financial Evidence Collection
          </p>
        </div>

        {investigation && (
          <div className="flex items-center gap-4 text-xs">
            <div className="text-right">
              <div className="text-muted-foreground">Confidence Score</div>
              <div className="text-xl font-bold text-emerald-400">
                {Math.round(investigation.confidenceScore ?? 0)}%
              </div>
            </div>
            <div className="text-right">
              <div className="text-muted-foreground">Analysis Source</div>
              <Badge variant="outline" className="text-purple-400 border-purple-500/30 text-[10px] mt-0.5">
                {investigation.analysisSource}
              </Badge>
            </div>
          </div>
        )}
      </div>

      {!investigation ? (
        <SectionCard>
          <div className="py-12 text-center space-y-4">
            <div className="w-12 h-12 rounded-xl bg-muted border border-border flex items-center justify-center text-foreground mx-auto">
              <Bot className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-foreground">No Investigation Report Found</h3>
              <p className="text-xs text-muted-foreground max-w-md mx-auto mt-1">
                An AI investigation has not been generated for exception #{exceptionId} yet. Click below to run Gemini AI analysis.
              </p>
            </div>
            {user?.role !== "OPERATOR" && (
              <Button
                size="sm"
                onClick={handleRunInvestigation}
                disabled={runInvestigation.isPending}
                className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
              >
                <Sparkles className="w-3.5 h-3.5 mr-1.5" />
                Trigger Gemini AI Investigation
              </Button>
            )}
          </div>
        </SectionCard>
      ) : (
        <>
          {/* Diagnostic Key Findings */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <SectionCard title="Likely Root Cause" description="Identified discrepancy trigger">
              <p className="text-xs text-muted-foreground leading-relaxed p-3 rounded-lg bg-muted/40 border border-border">
                {investigation.likelyRootCause}
              </p>
            </SectionCard>

            <SectionCard title="Recommended System Action" description="Next steps for financial resolution">
              <div className="p-3 rounded-lg bg-muted/40 border border-border space-y-2 text-xs">
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Recommendation:</span>
                  <span className="font-semibold text-amber-400">{investigation.recommendedAction}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Action Taken:</span>
                  <span className="font-medium text-foreground">{investigation.actionTaken}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Auto Resolved:</span>
                  <span className="font-medium text-foreground">{investigation.autoResolved ? "Yes" : "No"}</span>
                </div>
              </div>
            </SectionCard>
          </div>

          {/* Full Executive Summary */}
          <SectionCard title="AI Diagnostic Executive Summary" description="Structured report formatted by Gemini 1.5 Flash">
            <pre className="text-xs text-muted-foreground leading-relaxed whitespace-pre-wrap font-sans p-4 rounded-lg bg-muted/30 border border-border">
              {investigation.summary}
            </pre>
          </SectionCard>

          {/* Transaction Lineage */}
          <SectionCard title="Transaction Lineage Graph" description="Authoritative multi-party transaction chain from database">
            <TransactionLineage nodes={lineageNodes} />
          </SectionCard>

          {/* Evidence Graph & Sufficiency (Phase 3.5) */}
          {investigation.evidenceGraph && (
            <SectionCard
              title="Evidence Graph & Provenance"
              description={`Structured facts retrieved from database with provenance (${investigation.evidenceGraph.foundNodes} found, ${investigation.evidenceGraph.missingNodes} missing)`}
              actions={
                investigation.evidenceSufficiency && (
                  <Badge
                    variant="outline"
                    className={`text-xs ${
                      investigation.evidenceSufficiency.isSufficient || investigation.evidenceSufficiency.assessment === 'SUFFICIENT'
                        ? 'text-emerald-400 border-emerald-500/30'
                        : 'text-amber-400 border-amber-500/30'
                    }`}
                  >
                    Sufficiency: {Math.round(investigation.evidenceSufficiency.sufficiencyScore)}%
                    {investigation.evidenceSufficiency.sufficiencyLevel || investigation.evidenceSufficiency.assessment
                      ? ` (${investigation.evidenceSufficiency.sufficiencyLevel || investigation.evidenceSufficiency.assessment})`
                      : ''}
                  </Badge>
                )
              }
            >
              <div className="space-y-3">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                  {investigation.evidenceGraph.nodes.map((node, i) => (
                    <div key={i} className="p-3 rounded-lg bg-muted/30 border border-border text-xs space-y-1.5">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-foreground flex items-center gap-1.5">
                          <Layers className="w-3.5 h-3.5 text-muted-foreground" />
                          {node.entityType}
                        </span>
                        <Badge
                          variant="outline"
                          className={node.availability === "FOUND" ? "text-emerald-400 border-emerald-500/30 text-[10px]" : "text-amber-400 border-amber-500/30 text-[10px]"}
                        >
                          {node.availability}
                        </Badge>
                      </div>
                      <div className="font-mono text-[11px] text-muted-foreground truncate">
                        ID: {node.entityId || "N/A"}
                      </div>
                      <div className="text-[11px] text-muted-foreground">
                        Source: <span className="font-mono text-zinc-300">{node.source}</span>
                      </div>
                      {node.amount !== undefined && node.amount !== null && (
                        <div className="text-[11px] text-muted-foreground">
                          Amount: <span className="font-mono text-zinc-200">{formatCurrency(node.amount)}</span>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </SectionCard>
          )}

          {/* Historical Cases / RAG Evidence (Phase 6) */}
          <SectionCard
            title="Historical Cases / RAG Evidence"
            description="Vector & semantic retrieval over previously resolved investigations (merchant-isolated)"
          >
            {investigation.ragHistoricalCases && investigation.ragHistoricalCases.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {investigation.ragHistoricalCases.map((rc, idx) => (
                  <div key={idx} className="p-4 rounded-lg bg-muted/30 border border-border space-y-2 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-foreground font-semibold flex items-center gap-1.5">
                        <History className="w-3.5 h-3.5 text-purple-400" />
                        {rc.exceptionId}
                      </span>
                      <Badge variant="outline" className="text-purple-400 border-purple-500/30 text-[10px]">
                        {Math.round((rc.similarityScore ?? 0) * 100)}% Similarity
                      </Badge>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-[11px]">Type: </span>
                      <span className="font-mono text-zinc-300">{rc.exceptionType}</span>
                    </div>
                    {rc.discrepancyAmount && (
                      <div>
                        <span className="text-muted-foreground text-[11px]">Discrepancy: </span>
                        <span className="font-mono text-amber-400">{formatCurrency(rc.discrepancyAmount)}</span>
                      </div>
                    )}
                    <div className="pt-1 border-t border-border/50 space-y-1">
                      <p className="text-[11px] text-muted-foreground leading-snug">
                        <span className="font-medium text-zinc-300">Previous Root Cause:</span> {rc.previousRootCause}
                      </p>
                      <p className="text-[11px] text-muted-foreground leading-snug">
                        <span className="font-medium text-zinc-300">Resolution:</span> {rc.previousResolution}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="p-4 rounded-lg bg-muted/20 border border-border text-center text-xs text-muted-foreground">
                No past resolved cases matched the similarity threshold (0.50). Pure deterministic rules & ground truth evidence applied.
              </div>
            )}
          </SectionCard>

          {/* Forensic Hypotheses */}
          {investigation.hypotheses && investigation.hypotheses.length > 0 && (
            <SectionCard
              title="Forensic Diagnostic Hypotheses"
              description="Competing explanations evaluated against ground truth evidence"
            >
              <div className="space-y-3">
                {investigation.hypotheses.map((h, idx) => (
                  <div key={idx} className="p-4 rounded-lg bg-muted/30 border border-border space-y-2 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-foreground flex items-center gap-1.5">
                        <Lightbulb className="w-3.5 h-3.5 text-amber-400" />
                        Hypothesis {idx + 1}
                      </span>
                      <div className="flex items-center gap-2">
                        <Badge variant="outline" className="text-emerald-400 border-emerald-500/30 text-[10px]">
                          {Math.round(h.confidence)}% Confidence
                        </Badge>
                        <Badge variant="outline" className="text-zinc-300 text-[10px]">
                          {h.status}
                        </Badge>
                      </div>
                    </div>
                    <p className="text-muted-foreground">{h.hypothesis}</p>
                    {h.supportingEvidence && h.supportingEvidence.length > 0 && (
                      <div className="pt-1 border-t border-border/40 text-[11px]">
                        <span className="text-muted-foreground font-medium">Supporting Evidence: </span>
                        <ul className="list-disc list-inside mt-0.5 text-zinc-400 space-y-0.5">
                          {h.supportingEvidence.map((ev, i) => (
                            <li key={i}>{ev}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </SectionCard>
          )}

          {/* Contradictions (if present) */}
          {investigation.contradictions && investigation.contradictions.length > 0 && (
            <SectionCard
              title="Detected Contradictions"
              description="Conflicting evidence signals requiring reconciliation"
            >
              <div className="space-y-3">
                {investigation.contradictions.map((c, idx) => (
                  <div key={idx} className="p-4 rounded-lg bg-red-950/20 border border-red-500/20 space-y-1.5 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-red-400 flex items-center gap-1.5">
                        <AlertCircle className="w-3.5 h-3.5" />
                        Contradiction: {c.contradiction}
                      </span>
                      <Badge variant="outline" className="text-red-400 border-red-500/30 text-[10px]">
                        {c.severity}
                      </Badge>
                    </div>
                    <p className="text-muted-foreground">{c.resolution}</p>
                  </div>
                ))}
              </div>
            </SectionCard>
          )}
        </>
      )}
    </div>
  )
}
