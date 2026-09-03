import { useState } from "react"
import { useRunReconciliation } from "@/hooks/useReconciliation"
import { useAuth } from "@/contexts/AuthContext"
import {
  PageHeader,
  SectionCard,
  MetricCard,
  formatCurrency,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { toast } from "sonner"
import {
  CheckCircle2,
  AlertTriangle,
  Play,
  Layers,
  Activity,
} from "lucide-react"
import type { ReconciliationResult } from "@/types/api"

export default function ReconciliationPage() {
  const { user } = useAuth()
  const runReconciliation = useRunReconciliation()

  const [lastResult, setLastResult] = useState<ReconciliationResult | null>(null)

  const handleRunEngine = async () => {
    toast.promise(
      runReconciliation.mutateAsync().then((res) => {
        setLastResult(res)
        return res
      }),
      {
        loading: "Running Rules A-H Reconciliation Engine across database...",
        success: (res) =>
          `Reconciliation batch completed successfully! Checked ${res.recordsChecked} records and created ${res.exceptionsCreated} new exceptions.`,
        error: "Failed to run reconciliation process.",
      }
    )
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <PageHeader
        title="Multi-Way Reconciliation Engine"
        description="Audit multi-party transaction lineages against 8 deterministic reconciliation rules (Rules A–H)."
        actions={
          user?.role !== "OPERATOR" ? (
            <AlertDialog>
              <AlertDialogTrigger>
                <Button
                  size="sm"
                  disabled={runReconciliation.isPending}
                  className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
                >
                  <Play className="w-3.5 h-3.5 mr-1.5 fill-current" />
                  Run Global Reconciliation
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent className="bg-card border-border">
                <AlertDialogHeader>
                  <AlertDialogTitle className="text-foreground">
                    Confirm Reconciliation Engine Execution
                  </AlertDialogTitle>
                  <AlertDialogDescription className="text-muted-foreground text-xs">
                    This action will scan all orders, payment gateway charges, refund logs, and settlement batches against Rules A-H. Any discrepancies will automatically generate financial exception records.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel className="text-xs border-border">Cancel</AlertDialogCancel>
                  <AlertDialogAction
                    onClick={handleRunEngine}
                    className="text-xs bg-foreground hover:bg-foreground/90 text-background"
                  >
                    Start Execution
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          ) : undefined
        }
      />

      {/* Engine Status Banner */}
      <div className="p-4 rounded-xl border border-border bg-muted/30 flex flex-wrap items-center justify-between gap-4 text-xs">
        <div className="flex items-center gap-3">
          <div className="w-3 h-3 rounded-full bg-emerald-400 animate-pulse" />
          <div>
            <div className="font-semibold text-foreground">Engine Status: READY</div>
            <div className="text-muted-foreground mt-0.5">Deterministic Rules A–H · Multi-Way Reconciliation Engine</div>
          </div>
        </div>
        <Badge variant="outline" className="text-muted-foreground border-border text-[10px]">
          RULES A–H ACTIVE
        </Badge>
      </div>

      {/* Rules Overview Grid */}
      <SectionCard title="Deterministic Audit Rules (Rules A–H)" description="The 8 multi-way rules continuously evaluating database lineages">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
          {[
            { rule: "Rule A", name: "AMOUNT_MISMATCH", desc: "Settlement amount ≠ Expected Net Settlement" },
            { rule: "Rule B", name: "MISSING_SETTLEMENT", desc: "Payment success > 24h without settlement link" },
            { rule: "Rule C", name: "UNMATCHED_PAYMENT", desc: "Processor payment has no merchant order" },
            { rule: "Rule D", name: "DUPLICATE_PAYMENT", desc: "Multiple successful payments on same order ID" },
            { rule: "Rule E", name: "UNMATCHED_REFUND", desc: "Refund issued against missing/failed payment" },
            { rule: "Rule F", name: "FEE_OVERCHARGE", desc: "Gateway fees exceed contractual percentage" },
            { rule: "Rule G", name: "UNMATCHED_ADJUSTMENT", desc: "Manual adjustment lacks lineage mapping" },
            { rule: "Rule H", name: "CURRENCY_MISMATCH", desc: "ISO Currency mismatch across order & gateway" },
          ].map((item, i) => (
            <div key={i} className="p-3 rounded-lg border border-border bg-muted/30">
              <div className="flex items-center justify-between mb-1">
                <span className="font-mono text-[10px] text-muted-foreground font-medium">{item.rule}</span>
                <Badge variant="outline" className="text-[9px] text-muted-foreground">
                  ACTIVE
                </Badge>
              </div>
              <div className="font-semibold text-foreground mb-1">{item.name}</div>
              <p className="text-[11px] text-muted-foreground leading-tight">{item.desc}</p>
            </div>
          ))}
        </div>
      </SectionCard>

      {/* Last Run Results Display */}
      {lastResult && (
        <div className="space-y-6">
          <h2 className="text-sm font-semibold text-foreground">Latest Reconciliation Run Results</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <MetricCard
              title="Records Checked"
              value={lastResult.recordsChecked}
              icon={<Layers className="w-4 h-4" />}
              description="Orders, payments & settlements"
            />
            <MetricCard
              title="Successful Checks"
              value={lastResult.successfulChecks}
              icon={<CheckCircle2 className="w-4 h-4" />}
              accentColor="text-emerald-400"
              description="Lineages fully matched"
            />
            <MetricCard
              title="Exceptions Created"
              value={lastResult.exceptionsCreated}
              icon={<AlertTriangle className="w-4 h-4" />}
              accentColor="text-amber-400"
              description="New shortfalls detected"
            />
            <MetricCard
              title="Total Discrepancy Amount"
              value={formatCurrency(lastResult.totalDiscrepancyAmount)}
              icon={<Activity className="w-4 h-4" />}
              accentColor="text-red-400"
              description="Financial delta in run"
            />
          </div>

          {/* Items breakdown list */}
          {lastResult.items && lastResult.items.length > 0 && (
            <SectionCard title="Reconciliation Item Audit Log" description="Individual rule evaluation results from last run" noPadding>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr className="border-b border-border bg-muted/30 text-muted-foreground font-medium">
                      <th className="py-3 px-4">Item ID</th>
                      <th className="py-3 px-4">Type</th>
                      <th className="py-3 px-4">Rule Applied</th>
                      <th className="py-3 px-4">Result</th>
                      <th className="py-3 px-4">Message</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {lastResult.items.map((item, i) => (
                      <tr key={i} className="hover:bg-muted/40 transition-colors">
                        <td className="py-3 px-4 font-mono font-medium text-foreground">{item.itemId}</td>
                        <td className="py-3 px-4 text-muted-foreground">{item.itemType}</td>
                        <td className="py-3 px-4 font-mono text-muted-foreground">{item.ruleApplied}</td>
                        <td className="py-3 px-4">
                          {item.passed ? (
                            <span className="text-emerald-400 font-medium">PASSED</span>
                          ) : (
                            <span className="text-amber-400 font-medium">EXCEPTION CREATED</span>
                          )}
                        </td>
                        <td className="py-3 px-4 text-muted-foreground">{item.message}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </SectionCard>
          )}
        </div>
      )}
    </div>
  )
}
