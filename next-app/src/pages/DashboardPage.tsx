import { useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/contexts/AuthContext"
import { useDashboardStats } from "@/hooks/useDashboard"
import { useRunReconciliation } from "@/hooks/useReconciliation"
import {
  PageHeader,
  SectionCard,
  MetricCard,
  SeverityBadge,
  StatusBadge,
  LoadingState,
  ErrorState,
  EmptyState,
  formatCurrency,
  formatDateShort,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { toast } from "sonner"
import {
  RefreshCw,
  Play,
  AlertTriangle,
  TrendingUp,
  Activity,
  Layers,
} from "lucide-react"

export default function DashboardPage() {
  const navigate = useNavigate()
  const { user } = useAuth()

  const { data: stats, isLoading, isError, refetch } = useDashboardStats()
  const runReconciliation = useRunReconciliation()

  // ── Derived metrics ──────────────────────────────────────────────────────
  const derived = useMemo(() => {
    if (!stats) return null

    const totalSettled = stats.totalSettlementsAmount ?? 0
    const unreconciled = stats.unreconciledAmount ?? 0
    const reconciled = Math.max(0, totalSettled - unreconciled)

    const matchRateNum = totalSettled > 0 ? (reconciled / totalSettled) * 100 : 0
    const matchRate = matchRateNum.toFixed(1)

    const criticalCount = stats.severityBreakdown?.CRITICAL ?? 0
    const highCount = stats.severityBreakdown?.HIGH ?? 0

    return {
      unreconciled,
      matchRate,
      matchRateNum,
      criticalCount,
      highCount,
    }
  }, [stats])

  // ── Run reconciliation ───────────────────────────────────────────────────
  const handleRunRecon = () => {
    toast.promise(runReconciliation.mutateAsync(), {
      loading: "Running Rules A–H reconciliation engine…",
      success: (res) => {
        refetch()
        return `Completed — ${res.exceptionsCreated} new exception${res.exceptionsCreated !== 1 ? "s" : ""} detected.`
      },
      error: "Reconciliation failed. Check backend logs.",
    })
  }

  if (isLoading) return <LoadingState message="Loading dashboard statistics..." />
  if (isError || !stats || !derived) return <ErrorState title="Unable to load dashboard" onRetry={refetch} />

  const { unreconciled, matchRate, matchRateNum, criticalCount, highCount } = derived

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <PageHeader
        title="Operations Dashboard"
        description="Real-time monitoring of transaction reconciliation and financial exceptions."
        actions={
          <div className="flex items-center gap-3">
            <Button variant="outline" size="sm" onClick={() => refetch()} className="text-xs border-border">
              <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
              Refresh
            </Button>
            {user?.role !== "OPERATOR" && (
              <Button
                size="sm"
                onClick={handleRunRecon}
                disabled={runReconciliation.isPending}
                className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
              >
                <Play className="w-3.5 h-3.5 mr-1.5 fill-current" />
                Run Reconciliation
              </Button>
            )}
          </div>
        }
      />

      {/* Core KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Transactions Processed"
          value={(stats.totalTransactions ?? 0).toLocaleString()}
          icon={<Activity className="w-4 h-4" />}
          description={`${(stats.successfulPayments ?? 0).toLocaleString()} successful payments`}
        />

        <MetricCard
          title="Match Rate"
          value={`${matchRate}%`}
          icon={<TrendingUp className="w-4 h-4" />}
          accentColor={matchRateNum >= 99 ? "text-emerald-400" : "text-amber-400"}
          description={matchRateNum >= 99 ? "System operating nominally" : "Review recommended"}
        />

        <MetricCard
          title="Unreconciled Amount"
          value={formatCurrency(unreconciled)}
          icon={<Layers className="w-4 h-4" />}
          accentColor={unreconciled > 0 ? "text-amber-400" : "text-emerald-400"}
          description={unreconciled > 0 ? "Requires attention" : "All transactions reconciled"}
        />

        <MetricCard
          title="Critical Exceptions"
          value={criticalCount}
          icon={<AlertTriangle className="w-4 h-4" />}
          accentColor={criticalCount > 0 ? "text-rose-400" : "text-emerald-400"}
          description={criticalCount > 0 ? `${highCount} HIGH severity active` : "No critical issues"}
        />
      </div>

      {/* Reconciliation Health */}
      <SectionCard
        title="Reconciliation Health"
        description="Multi-way transaction lineage audit status"
        actions={
          <Badge variant="outline" className="text-muted-foreground border-border text-[10px]">
            RULES A–H ACTIVE
          </Badge>
        }
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-lg border border-border bg-muted/30">
            <p className="text-xs text-muted-foreground mb-1">Total Settlements</p>
            <p className="text-lg font-semibold text-foreground font-mono">{formatCurrency(stats.totalSettlementsAmount ?? 0)}</p>
            <p className="text-xs text-muted-foreground mt-1">{(stats.totalSettlements ?? 0)} batches</p>
          </div>
          <div className="p-4 rounded-lg border border-border bg-muted/30">
            <p className="text-xs text-muted-foreground mb-1">Total Refunds</p>
            <p className="text-lg font-semibold text-foreground font-mono">{formatCurrency(stats.totalRefundsAmount ?? 0)}</p>
            <p className="text-xs text-muted-foreground mt-1">{(stats.refundsCount ?? 0)} issued</p>
          </div>
          <div className="p-4 rounded-lg border border-border bg-muted/30">
            <p className="text-xs text-muted-foreground mb-1">Gateway Fees</p>
            <p className="text-lg font-semibold text-foreground font-mono">{formatCurrency(stats.totalFeesAmount ?? 0)}</p>
            <p className="text-xs text-muted-foreground mt-1">Total collected</p>
          </div>
        </div>
      </SectionCard>

      {/* Recent Exceptions */}
      <SectionCard
        title="Recent Exceptions"
        description="Latest detected financial discrepancies"
        actions={
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate("/exceptions")}
            className="text-xs border-border"
          >
            View All
          </Button>
        }
        noPadding
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border bg-muted/30 text-muted-foreground font-medium">
                <th className="py-3 px-4">Exception ID</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Discrepancy</th>
                <th className="py-3 px-4">Detected</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {stats.recentExceptions && stats.recentExceptions.length > 0 ? (
                stats.recentExceptions.map((exp) => (
                  <tr
                    key={exp.exceptionId}
                    className="hover:bg-muted/40 transition-colors cursor-pointer"
                    onClick={() => navigate(`/exceptions/${exp.exceptionId}`)}
                  >
                    <td className="py-3.5 px-4 font-mono font-medium text-foreground">
                      {exp.exceptionId}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground">
                      {exp.exceptionType.replace(/_/g, " ")}
                    </td>
                    <td className="py-3.5 px-4">
                      <SeverityBadge severity={exp.severity} />
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={exp.status} />
                    </td>
                    <td className="py-3.5 px-4 font-mono font-semibold text-amber-400">
                      {formatCurrency(exp.discrepancyAmount)}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground">
                      {formatDateShort(exp.detectedAt)}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-[11px] h-7 border-border hover:bg-muted hover:text-foreground transition-all"
                        onClick={(e: React.MouseEvent) => {
                          e.stopPropagation()
                          navigate(`/exceptions/${exp.exceptionId}`)
                        }}
                      >
                        Investigate
                      </Button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-12">
                    <EmptyState
                      icon={<AlertTriangle className="w-6 h-6" />}
                      title="No recent exceptions"
                      description="All transactions are currently reconciled. Run reconciliation to check for new discrepancies."
                    />
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </SectionCard>
    </div>
  )
}
