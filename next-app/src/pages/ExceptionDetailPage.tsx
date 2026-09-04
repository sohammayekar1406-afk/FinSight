import { useParams, useNavigate } from "react-router-dom"
import { useExceptionDetail } from "@/hooks/useExceptions"
import {
  useInvestigationDetail,
  useRunInvestigation,
  useResolveException,
} from "@/hooks/useInvestigations"
import { useAuth } from "@/contexts/AuthContext"
import {
  SectionCard,
  SeverityBadge,
  StatusBadge,
  ExceptionTypeBadge,
  LoadingState,
  ErrorState,
  TransactionLineage,
  formatCurrency,
  formatDate,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { toast } from "sonner"
import {
  ArrowLeft,
  Bot,
  CheckCircle2,
  RefreshCw,
  Search,
  FileText,
  ArrowRight,
} from "lucide-react"

export default function ExceptionDetailPage() {
  const { exceptionId } = useParams<{ exceptionId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()

  const { data: exception, isLoading: isExpLoading, isError: isExpError, refetch } = useExceptionDetail(exceptionId)
  const { data: investigation, refetch: refetchInv } = useInvestigationDetail(exceptionId)

  const runInvestigation = useRunInvestigation()
  const resolveException = useResolveException()

  const handleTriggerInvestigation = async () => {
    if (!exceptionId) return
    toast.promise(runInvestigation.mutateAsync(exceptionId), {
      loading: "Gathering evidence and querying Gemini AI engine...",
      success: (res) => {
        refetch()
        refetchInv()
        return `Investigation complete: Analysis source is ${res.analysisSource}. Root cause identified with ${Math.round(res.confidenceScore ?? 0)}% confidence.`
      },
      error: "Failed to complete AI investigation.",
    })
  }

  const handleResolve = async () => {
    if (!exceptionId) return
    toast.promise(resolveException.mutateAsync(exceptionId), {
      loading: "Applying manual resolution override...",
      success: () => {
        refetch()
        refetchInv()
        return "Exception status updated to RESOLVED_MANUAL and logged to audit trail."
      },
      error: "Only ADMIN role can execute manual resolutions.",
    })
  }

  if (isExpLoading) return <LoadingState message="Loading exception details & evidence workspace..." />
  if (isExpError || !exception) return <ErrorState title="Exception not found" message={`Could not load record #${exceptionId}`} onRetry={refetch} />

  // Lineage nodes for visualization
  const lineageNodes = [
    { id: "ord", label: `Order ${exception.orderId || "—"}`, value: exception.expectedAmount ? formatCurrency(exception.expectedAmount) : undefined, status: "normal" as const },
    { id: "pay", label: `Payment ${exception.paymentId || "—"}`, value: exception.actualAmount ? formatCurrency(exception.actualAmount) : undefined, status: "normal" as const },
    { id: "set", label: `Settlement ${exception.settlementId || "—"}`, value: exception.actualAmount ? formatCurrency(exception.actualAmount) : undefined, status: "normal" as const },
    { id: "exp", label: `${exception.exceptionType}`, value: `Shortfall ${formatCurrency(exception.discrepancyAmount)}`, status: exception.status.startsWith("RESOLVED") ? ("resolved" as const) : ("exception" as const) },
  ]

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Bar with Back Button */}
      <div className="flex items-center justify-between">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate("/exceptions")}
          className="text-xs text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="w-3.5 h-3.5 mr-1" />
          Back to Exceptions
        </Button>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              refetch()
              refetchInv()
            }}
            className="text-xs border-border"
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Refresh
          </Button>

          {user?.role !== "OPERATOR" && (
            <Button
              size="sm"
              onClick={handleTriggerInvestigation}
              disabled={runInvestigation.isPending}
              className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
            >
              <Bot className="w-3.5 h-3.5 mr-1.5" />
              {investigation ? "Re-run Investigation" : "Run AI Investigation"}
            </Button>
          )}

          {user?.role === "ADMIN" && exception.status !== "RESOLVED_MANUAL" && (
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

      {/* Main Workspace Header */}
      <div className="p-6 rounded-xl border border-border bg-card flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-mono text-lg font-bold text-foreground">{exception.exceptionId}</span>
            <ExceptionTypeBadge type={exception.exceptionType} />
            <SeverityBadge severity={exception.severity} />
            <StatusBadge status={exception.status} />
          </div>
          <p className="text-xs text-muted-foreground">{exception.description}</p>
        </div>

        <div className="text-right">
          <div className="text-xs text-muted-foreground">Discrepancy Amount</div>
          <div className="text-2xl font-bold font-mono text-amber-400">
            {formatCurrency(exception.discrepancyAmount)}
          </div>
        </div>
      </div>

      {/* 3-Column Layout: Exception Details | Discrepancy Breakdown | Investigation Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* LEFT: Exception Info */}
        <SectionCard title="Exception Parameters" description="Key entity identifiers and timestamps">
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">Merchant ID:</span>
              <span className="font-mono font-medium text-foreground">{exception.merchantId}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">Order ID:</span>
              <span className="font-mono text-foreground">{exception.orderId || "N/A"}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">Payment ID:</span>
              <span className="font-mono text-foreground">{exception.paymentId || "N/A"}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">Settlement ID:</span>
              <span className="font-mono text-foreground">{exception.settlementId || "N/A"}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-border/50">
              <span className="text-muted-foreground">Detected At:</span>
              <span className="text-foreground">{formatDate(exception.detectedAt)}</span>
            </div>
          </div>
        </SectionCard>

        {/* CENTER: Discrepancy Amount Card */}
        <SectionCard title="Financial Discrepancy" description="Expected vs Actual settlement amounts">
          <div className="space-y-4 text-xs">
            <div className="p-3 rounded-lg bg-muted/40 border border-border flex justify-between items-center">
              <span className="text-muted-foreground">Expected Net Settlement:</span>
              <span className="font-mono text-sm font-semibold text-emerald-400">
                {formatCurrency(exception.expectedAmount)}
              </span>
            </div>
            <div className="p-3 rounded-lg bg-muted/40 border border-border flex justify-between items-center">
              <span className="text-muted-foreground">Actual Settled Amount:</span>
              <span className="font-mono text-sm font-semibold text-zinc-300">
                {formatCurrency(exception.actualAmount)}
              </span>
            </div>
            <div className="p-3 rounded-lg bg-red-950/20 border border-red-500/20 flex justify-between items-center">
              <span className="text-red-400 font-medium">Discrepancy Shortfall:</span>
              <span className="font-mono text-base font-bold text-red-400">
                {formatCurrency(exception.discrepancyAmount)}
              </span>
            </div>
          </div>
        </SectionCard>

        {/* RIGHT: AI Investigation Status Card */}
        <SectionCard title="AI Diagnostic Status" description="Gemini 1.5 Flash analysis status">
          {investigation ? (
            <div className="space-y-3 text-xs">
              <div className="flex justify-between items-center py-1 border-b border-border/50">
                <span className="text-muted-foreground">Analysis Source:</span>
                <Badge variant="outline" className="text-[10px] text-purple-400 border-purple-500/30">
                  {investigation.analysisSource}
                </Badge>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-border/50">
                <span className="text-muted-foreground">Confidence Score:</span>
                <span className="font-semibold text-emerald-400">
                  {Math.round(investigation.confidenceScore ?? 0)}%
                </span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-border/50">
                <span className="text-muted-foreground">Recommended Action:</span>
                <span className="font-medium text-amber-400">{investigation.recommendedAction}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-border/50">
                <span className="text-muted-foreground">Action Taken:</span>
                <span className="font-medium text-foreground">{investigation.actionTaken}</span>
              </div>
            </div>
          ) : (
            <div className="py-6 text-center space-y-3">
              <p className="text-xs text-muted-foreground">No AI investigation has been run on this exception yet.</p>
              {user?.role !== "OPERATOR" && (
                <Button
                  size="sm"
                  onClick={handleTriggerInvestigation}
                  disabled={runInvestigation.isPending}
                  className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
                >
                  <Bot className="w-3.5 h-3.5 mr-1.5" />
                  Run AI Diagnosis Now
                </Button>
              )}
            </div>
          )}
        </SectionCard>
      </div>

      {/* Transaction Lineage Visualization */}
      <SectionCard title="Transaction Lineage Graph" description="End-to-end multi-way transaction chain">
        <TransactionLineage nodes={lineageNodes} />
      </SectionCard>

      {/* AI Investigation Detailed Analysis Section */}
      {investigation && (
        <SectionCard
          title="AI Investigation Report"
          description={`Generated on ${formatDate(investigation.investigatedAt)} via ${investigation.analysisSource}`}
          actions={
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate(`/investigations/${exceptionId}`)}
              className="text-xs text-muted-foreground hover:text-foreground"
            >
              Full Investigation View
              <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          }
        >
          <div className="space-y-4 text-xs">
            <div className="p-4 rounded-lg bg-muted/30 border border-border space-y-1.5">
              <div className="font-semibold text-foreground flex items-center gap-1.5">
                <Search className="w-3.5 h-3.5 text-muted-foreground" />
                Likely Root Cause
              </div>
              <p className="text-muted-foreground leading-relaxed">{investigation.likelyRootCause}</p>
            </div>

            <div className="p-4 rounded-lg bg-muted/30 border border-border space-y-1.5">
              <div className="font-semibold text-foreground flex items-center gap-1.5">
                <FileText className="w-3.5 h-3.5 text-muted-foreground" />
                Executive Summary
              </div>
              <pre className="text-muted-foreground leading-relaxed whitespace-pre-wrap font-sans text-xs">
                {investigation.summary}
              </pre>
            </div>
          </div>
        </SectionCard>
      )}
    </div>
  )
}
