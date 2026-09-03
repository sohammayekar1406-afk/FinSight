import { useState } from "react"
import { useAuditLogsPaged } from "@/hooks/useAuditLogs"
import {
  PageHeader,
  SectionCard,
  LoadingState,
  ErrorState,
  EmptyState,
  formatDate,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  ScrollText,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Shield,
  User,
} from "lucide-react"

export default function AuditLogsPage() {
  const [page, setPage] = useState(0)
  const pageSize = 20

  const { data: pagedData, isLoading, isError, refetch } = useAuditLogsPaged(page, pageSize)

  if (isLoading) return <LoadingState message="Loading immutable audit trail logs..." />
  if (isError) return <ErrorState title="Unable to fetch audit logs" onRetry={refetch} />

  const auditLogs = pagedData?.content || []
  const totalPages = pagedData?.totalPages || 1

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <PageHeader
        title="Audit Logs & Traceability"
        description="Immutable system audit log capturing reconciliation runs, AI diagnostic events, and manual analyst overrides."
        actions={
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="text-xs border-border"
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Refresh Log Stream
          </Button>
        }
      />

      {/* Security Banner */}
      <div className="p-4 rounded-xl border border-border bg-card flex items-center justify-between gap-4 text-xs">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-muted border border-border flex items-center justify-center text-foreground">
            <Shield className="w-4 h-4" />
          </div>
          <div>
            <div className="font-semibold text-foreground">Tamper-Proof System Audit Trail</div>
            <div className="text-muted-foreground mt-0.5">
              All events are timestamped and bound to user credentials via Spring Security.
            </div>
          </div>
        </div>
        <Badge variant="outline" className="text-emerald-400 border-emerald-500/30 text-[10px]">
          LOGGING VERIFIED
        </Badge>
      </div>

      {/* Audit Logs Table */}
      <SectionCard noPadding>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border bg-muted/30 text-muted-foreground font-medium">
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4">Action</th>
                <th className="py-3 px-4">Entity Type</th>
                <th className="py-3 px-4">Entity ID</th>
                <th className="py-3 px-4">Performed By</th>
                <th className="py-3 px-4">Details JSON</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border font-mono text-[11px]">
              {auditLogs.length > 0 ? (
                auditLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-muted/40 transition-colors">
                    <td className="py-3 px-4 text-muted-foreground whitespace-nowrap">
                      {formatDate(log.createdAt)}
                    </td>
                    <td className="py-3 px-4 font-semibold text-foreground font-sans">
                      {log.action}
                    </td>
                    <td className="py-3 px-4 text-zinc-300 font-sans">{log.entityType}</td>
                    <td className="py-3 px-4 text-zinc-400 truncate max-w-[150px]">
                      {log.entityId}
                    </td>
                    <td className="py-3 px-4 font-sans font-medium text-foreground">
                      <span className="inline-flex items-center gap-1">
                        <User className="w-3 h-3 text-muted-foreground" />
                        {log.performedBy}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-muted-foreground truncate max-w-[300px]">
                      {log.details || "—"}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-12">
                    <EmptyState
                      icon={<ScrollText className="w-6 h-6" />}
                      title="No audit entries recorded"
                      description="Audit events will appear here as reconciliation engine and AI investigations are executed."
                    />
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Footer Pagination */}
        <div className="p-4 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
          <div>
            Showing Page <span className="font-semibold text-foreground">{page + 1}</span> of{" "}
            <span className="font-semibold text-foreground">{totalPages}</span> (Total{" "}
            <span className="font-semibold text-foreground">{pagedData?.totalElements || 0}</span> audit logs)
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="h-8 text-xs border-border"
            >
              <ChevronLeft className="w-3.5 h-3.5 mr-1" />
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="h-8 text-xs border-border"
            >
              Next
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          </div>
        </div>
      </SectionCard>
    </div>
  )
}
