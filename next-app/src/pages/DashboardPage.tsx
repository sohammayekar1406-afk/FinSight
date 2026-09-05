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
    if (!stats || typeof stats !== "object") {
      return {
        hasReconciled: false,
        unreconciled: 0,
        matchRate: "—",
        matchRateNum: 0,
        criticalCount: 0,
        highCount: 0,
      }
    }

    const hasReconciled = Boolean(stats.hasReconciled)
    const totalSettled = Number(stats.totalSettlementsAmount) || 0
    const unreconciled = Number(stats.unreconciledAmount) || 0
    const reconciled = Math.max(0, totalSettled - unreconciled)

    const matchRateNum = hasReconciled && totalSettled > 0 ? (reconciled / totalSettled) * 100 : 0
    const matchRate = hasReconciled ? matchRateNum.toFixed(1) : "—"

    const criticalCount = stats.severityBreakdown?.CRITICAL ?? 0
    const highCount = stats.severityBreakdown?.HIGH ?? 0

    return {
      hasReconciled,
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
  if (isError || !stats) return <ErrorState title="Unable to load dashboard" onRetry={refetch} />

  const { hasReconciled, unreconciled, matchRate, matchRateNum, criticalCount, highCount } = derived

  return (
    <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8">
      {/* Top Header */}
      <PageHeader
        title="Operations Dashboard"
        description="Real-time monitoring of transaction reconciliation and financial exceptions."
        actions={
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              className="text-xs border-border/80 hover:bg-muted/60 hover:text-foreground transition-all duration-150"
            >
              <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
              Refresh
            </Button>
            {user?.role !== "OPERATOR" && (
              <Button
                size="sm"
                onClick={handleRunRecon}
                disabled={runReconciliation.isPending}
                className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium shadow-sm hover:shadow transition-all duration-150 active:scale-[0.98]"
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
          icon={<Activity className="w-4 h-4 text-emerald-400" />}
          accentColor="text-emerald-400"
          description={`${(stats.successfulPayments ?? 0).toLocaleString()} successful payments`}
        />

        <MetricCard
          title="Match Rate"
          value={hasReconciled ? `${matchRate}%` : "—"}
          icon={<TrendingUp className={`w-4 h-4 ${!hasReconciled ? "text-muted-foreground" : matchRateNum >= 99 ? "text-emerald-400" : "text-amber-400"}`} />}
          accentColor={!hasReconciled ? "text-muted-foreground" : matchRateNum >= 99 ? "text-emerald-400" : "text-amber-400"}
          description={
            hasReconciled ? (
              <span className="flex items-center gap-1.5">
                <span className={`w-1.5 h-1.5 rounded-full ${matchRateNum >= 99 ? "bg-emerald-400" : "bg-amber-400 animate-pulse"}`} />
                <span className={matchRateNum >= 99 ? "text-emerald-400/90" : "text-amber-400 font-semibold"}>
                  {matchRateNum >= 99 ? "System operating nominally" : "Review recommended"}
                </span>
              </span>
            ) : (
              <span className="flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground/40" />
                <span className="text-muted-foreground/80">Reconciliation not yet run</span>
              </span>
            )
          }
        />

        <MetricCard
          title="Unreconciled Amount"
          value={hasReconciled ? formatCurrency(unreconciled) : "—"}
          icon={<Layers className={`w-4 h-4 ${!hasReconciled ? "text-muted-foreground" : unreconciled > 0 ? "text-amber-400" : "text-emerald-400"}`} />}
          accentColor={!hasReconciled ? "text-muted-foreground" : unreconciled > 0 ? "text-amber-400" : "text-emerald-400"}
          description={
            hasReconciled ? (
              <span className="flex items-center gap-1.5">
                <span className={`w-1.5 h-1.5 rounded-full ${unreconciled > 0 ? "bg-amber-400 animate-pulse" : "bg-emerald-400"}`} />
                <span className={unreconciled > 0 ? "text-amber-400 font-semibold" : "text-emerald-400/90"}>
                  {unreconciled > 0 ? "Requires attention" : "All transactions reconciled"}
                </span>
              </span>
            ) : (
              <span className="flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground/40" />
                <span className="text-muted-foreground/80">Pending reconciliation run</span>
              </span>
            )
          }
        />

        <MetricCard
          title="Critical Exceptions"
          value={hasReconciled ? criticalCount : 0}
          icon={<AlertTriangle className={`w-4 h-4 ${!hasReconciled ? "text-muted-foreground" : criticalCount > 0 ? "text-rose-400" : "text-emerald-400"}`} />}
          accentColor={!hasReconciled ? "text-muted-foreground" : criticalCount > 0 ? "text-rose-400" : "text-emerald-400"}
          description={
            hasReconciled ? (
              <span className="flex items-center gap-1.5">
                <span className={`w-1.5 h-1.5 rounded-full ${criticalCount > 0 ? "bg-rose-500 animate-pulse" : "bg-emerald-400"}`} />
                <span className={criticalCount > 0 ? "text-rose-400 font-semibold" : "text-emerald-400/90"}>
                  {criticalCount > 0 ? `${highCount} HIGH severity active` : "No critical issues"}
                </span>
              </span>
            ) : (
              <span className="flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground/40" />
                <span className="text-muted-foreground/80">Reconciliation pending</span>
              </span>
            )
          }
        />
      </div>

      {/* Reconciliation Health */}
      <SectionCard
        title="Reconciliation Health"
        description="Multi-way transaction lineage audit status"
        actions={
          <Badge variant="outline" className="text-zinc-300 border-border/80 bg-muted/40 text-[10px] font-mono tracking-wide">
            {hasReconciled ? "RULES A–H ACTIVE" : "PENDING RECONCILIATION"}
          </Badge>
        }
      >
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Bar chart visualization */}
          <div className="space-y-4">
            {(() => {
              const settlementsAmount = stats.totalSettlementsAmount ?? 0
              const refundsAmount = stats.totalRefundsAmount ?? 0
              const feesAmount = stats.totalFeesAmount ?? 0
              const maxValue = Math.max(settlementsAmount, refundsAmount, feesAmount) || 1

              return (
                <>
                  <div className="flex items-end justify-between gap-3 h-32 w-full">
                    {/* Settlements bar */}
                    <div className="flex-1 flex flex-col items-center justify-end gap-2 h-full min-w-0">
                      <span className="text-[11px] text-emerald-400 font-mono font-semibold">
                        {formatCurrency(settlementsAmount)}
                      </span>
                      <div
                        className="w-full max-w-full bg-gradient-to-t from-emerald-500 to-emerald-400 rounded-t transition-all duration-500 hover:opacity-80"
                        style={{ height: `${(settlementsAmount / maxValue) * 100}%` }}
                      />
                      <span className="text-[10px] text-muted-foreground/70 font-medium mt-1">Settlements</span>
                    </div>

                    {/* Refunds bar */}
                    <div className="flex-1 flex flex-col items-center justify-end gap-2 h-full min-w-0">
                      <span className="text-[11px] text-amber-400 font-mono font-semibold">
                        {formatCurrency(refundsAmount)}
                      </span>
                      <div
                        className="w-full max-w-full bg-gradient-to-t from-amber-500 to-amber-400 rounded-t transition-all duration-500 hover:opacity-80"
                        style={{ height: `${(refundsAmount / maxValue) * 100}%` }}
                      />
                      <span className="text-[10px] text-muted-foreground/70 font-medium mt-1">Refunds</span>
                    </div>

                    {/* Fees bar */}
                    <div className="flex-1 flex flex-col items-center justify-end gap-2 h-full min-w-0">
                      <span className="text-[11px] text-zinc-400 font-mono font-semibold">
                        {formatCurrency(feesAmount)}
                      </span>
                      <div
                        className="w-full max-w-full bg-gradient-to-t from-zinc-500 to-zinc-400 rounded-t transition-all duration-500 hover:opacity-80"
                        style={{ height: `${(feesAmount / maxValue) * 100}%` }}
                      />
                      <span className="text-[10px] text-muted-foreground/70 font-medium mt-1">Fees</span>
                    </div>
                  </div>
                </>
              )
            })()}
          </div>

          {/* Metric cards */}
          <div className="grid grid-cols-1 gap-4">
            <div className="p-4 rounded-xl border border-border/80 bg-gradient-to-b from-card to-card/80 relative overflow-hidden space-y-1.5">
              <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />
              <p className="text-[11px] font-semibold text-muted-foreground/70 uppercase tracking-wider">Total Settlements</p>
              <p className="text-2xl font-bold text-foreground font-mono tracking-tighter">{formatCurrency(stats.totalSettlementsAmount ?? 0)}</p>
              <p className="text-xs text-muted-foreground/70">{(stats.totalSettlements ?? 0)} batches</p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="p-3 rounded-xl border border-border/80 bg-gradient-to-b from-card to-card/80 relative overflow-hidden space-y-1">
                <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />
                <p className="text-[10px] font-semibold text-muted-foreground/70 uppercase tracking-wider">Refunds</p>
                <p className="text-lg font-bold text-foreground font-mono tracking-tighter">{formatCurrency(stats.totalRefundsAmount ?? 0)}</p>
              </div>
              <div className="p-3 rounded-xl border border-border/80 bg-gradient-to-b from-card to-card/80 relative overflow-hidden space-y-1">
                <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />
                <p className="text-[10px] font-semibold text-muted-foreground/70 uppercase tracking-wider">Fees</p>
                <p className="text-lg font-bold text-foreground font-mono tracking-tighter">{formatCurrency(stats.totalFeesAmount ?? 0)}</p>
              </div>
            </div>
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
            className="text-xs border-border/80 hover:bg-muted/60 hover:text-foreground transition-all duration-150"
          >
            View All
          </Button>
        }
        noPadding
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border/80 bg-muted/40 text-muted-foreground/70 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Exception ID</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Discrepancy</th>
                <th className="py-3 px-4">Detected</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {stats.recentExceptions && stats.recentExceptions.length > 0 ? (
                stats.recentExceptions.map((exp) => (
                  <tr
                    key={exp.exceptionId}
                    className="even:bg-muted/15 hover:bg-muted/35 transition-colors duration-150 cursor-pointer"
                    onClick={() => navigate(`/exceptions/${exp.exceptionId}`)}
                  >
                    <td className="py-3.5 px-4 font-mono font-medium text-foreground">
                      {exp.exceptionId}
                    </td>
                    <td className="py-3.5 px-4 text-zinc-300">
                      {exp.exceptionType.replace(/_/g, " ")}
                    </td>
                    <td className="py-3.5 px-4">
                      <SeverityBadge severity={exp.severity} />
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={exp.status} />
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono font-semibold text-amber-400">
                      {formatCurrency(exp.discrepancyAmount)}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground">
                      {formatDateShort(exp.detectedAt)}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-[11px] h-7 border-border/80 hover:bg-muted/60 hover:text-foreground transition-all duration-150"
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
