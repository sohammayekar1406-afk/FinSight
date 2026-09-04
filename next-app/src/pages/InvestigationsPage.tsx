import React, { useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { useExceptions } from "@/hooks/useExceptions"
import { useRunAllInvestigations } from "@/hooks/useInvestigations"
import { useAuth } from "@/contexts/AuthContext"
import type { FinancialException } from "@/types/api"
import {
  PageHeader,
  SectionCard,
  SeverityBadge,
  StatusBadge,
  ExceptionTypeBadge,
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
  Bot,
  RefreshCw,
  ArrowRight,
  Sparkles,
} from "lucide-react"

export default function InvestigationsPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { data: rawExceptions, isLoading, isError, refetch } = useExceptions()
  const runAll = useRunAllInvestigations()

  // Safely extract exceptions array (unwrapping { data: [...] } or { content: [...] })
  const exceptions: FinancialException[] = useMemo(() => {
    if (Array.isArray(rawExceptions)) return rawExceptions
    if (rawExceptions && typeof rawExceptions === "object") {
      if (Array.isArray((rawExceptions as Record<string, unknown>).data)) {
        return (rawExceptions as { data: FinancialException[] }).data
      }
      if (Array.isArray((rawExceptions as Record<string, unknown>).content)) {
        return (rawExceptions as { content: FinancialException[] }).content
      }
    }
    return []
  }, [rawExceptions])

  const handleRunAll = async () => {
    toast.promise(runAll.mutateAsync(), {
      loading: "Running AI investigations for all open financial exceptions...",
      success: (res) => {
        refetch()
        return `Batch AI investigation completed: ${res.investigationsCreated} processed, ${res.autoResolved} auto-resolved, ${res.sentToHuman} sent to human analyst.`
      },
      error: "Failed to complete batch AI investigation.",
    })
  }

  if (isLoading) return <LoadingState message="Loading AI investigation records..." />
  if (isError) return <ErrorState title="Unable to load investigation records" onRetry={refetch} />

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <PageHeader
        title="AI Exception Investigations"
        description="Root cause diagnostics powered by Gemini 1.5 Flash with deterministic rule-based fallback."
        actions={
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              className="text-xs border-border"
            >
              <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
              Refresh
            </Button>
            {user?.role !== "OPERATOR" && (
              <Button
                size="sm"
                onClick={handleRunAll}
                disabled={runAll.isPending}
                className="text-xs bg-foreground hover:bg-foreground/90 text-background font-medium"
              >
                <Sparkles className="w-3.5 h-3.5 mr-1.5" />
                Investigate All Open Exceptions
              </Button>
            )}
          </div>
        }
      />

      {/* Banner */}
      <div className="p-4 rounded-xl border border-purple-500/20 bg-purple-500/5 flex items-center justify-between gap-4 text-xs">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-400">
            <Bot className="w-4 h-4" />
          </div>
          <div>
            <div className="font-semibold text-foreground">Zero-Hallucination AI Architecture</div>
            <div className="text-muted-foreground mt-0.5">
              Gemini 1.5 Flash outputs are validated against PostgreSQL ground truth before persistence.
            </div>
          </div>
        </div>
        <Badge variant="outline" className="text-purple-400 border-purple-500/30 text-[10px]">
          GEMINI 1.5 FLASH + RULE FALLBACK
        </Badge>
      </div>

      {/* Investigations Table */}
      <SectionCard noPadding>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border bg-muted/30 text-muted-foreground/70 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Exception ID</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Discrepancy</th>
                <th className="py-3 px-4">Detected At</th>
                <th className="py-3 px-4 text-right">Investigation Workstation</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {Array.isArray(exceptions) && exceptions.length > 0 ? (
                exceptions.map((exp) => (
                  <tr
                    key={exp.id || exp.exceptionId}
                    className="even:bg-muted/15 hover:bg-muted/35 transition-colors duration-150 cursor-pointer"
                    onClick={() => navigate(`/investigations/${exp.exceptionId}`)}
                  >
                    <td className="py-3.5 px-4 font-mono font-medium text-foreground">
                      {exp.exceptionId}
                    </td>
                    <td className="py-3.5 px-4">
                      <ExceptionTypeBadge type={exp.exceptionType} />
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
                        variant="ghost"
                        className="text-xs text-muted-foreground hover:text-foreground"
                        onClick={(e: React.MouseEvent) => {
                          e.stopPropagation()
                          navigate(`/investigations/${exp.exceptionId}`)
                        }}
                      >
                        View AI Report
                        <ArrowRight className="w-3.5 h-3.5 ml-1" />
                      </Button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-12">
                    <EmptyState
                      icon={<Bot className="w-6 h-6" />}
                      title="No investigation records"
                      description="Run reconciliation first to detect exceptions and generate AI investigation reports."
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
