import React, { useState, useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { useExceptions } from "@/hooks/useExceptions"
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
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  AlertTriangle,
  Search,
  Filter,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
} from "lucide-react"

export default function ExceptionsPage() {
  const navigate = useNavigate()
  const { data: rawExceptions, isLoading, isError, refetch } = useExceptions()

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

  // Filter states
  const [searchTerm, setSearchTerm] = useState("")
  const [severityFilter, setSeverityFilter] = useState<string>("ALL")
  const [statusFilter, setStatusFilter] = useState<string>("ALL")
  const [typeFilter, setTypeFilter] = useState<string>("ALL")

  // Pagination state
  const [page, setPage] = useState(0)
  const pageSize = 10

  // Filtered & Sorted exceptions
  const filteredExceptions = useMemo(() => {
    if (!Array.isArray(exceptions)) return []
    return exceptions.filter((exp) => {
      // Search
      const matchesSearch =
        !searchTerm ||
        exp.exceptionId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        exp.merchantId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        exp.orderId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        exp.paymentId?.toLowerCase().includes(searchTerm.toLowerCase())

      // Severity filter
      const matchesSeverity = severityFilter === "ALL" || exp.severity === severityFilter

      // Status filter
      const matchesStatus = statusFilter === "ALL" || exp.status === statusFilter

      // Type filter
      const matchesType = typeFilter === "ALL" || exp.exceptionType === typeFilter

      return matchesSearch && matchesSeverity && matchesStatus && matchesType
    })
  }, [exceptions, searchTerm, severityFilter, statusFilter, typeFilter])

  // Paginated slice
  const paginatedExceptions = useMemo(() => {
    const start = page * pageSize
    return filteredExceptions.slice(start, start + pageSize)
  }, [filteredExceptions, page, pageSize])

  const totalPages = Math.ceil(filteredExceptions.length / pageSize) || 1

  if (isLoading) return <LoadingState message="Fetching financial exceptions..." />
  if (isError) return <ErrorState title="Unable to load exceptions" onRetry={refetch} />

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header */}
      <PageHeader
        title="Financial Exceptions"
        description="Filter, audit, and launch investigations on detected ledger discrepancies."
        actions={
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="text-xs border-border"
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Refresh
          </Button>
        }
      />

      {/* Filter Controls Bar */}
      <div className="p-4 rounded-xl border border-border bg-card flex flex-col md:flex-row items-stretch md:items-center gap-3">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search Exception ID, Order ID, Merchant..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value)
              setPage(0)
            }}
            className="pl-9 h-9 text-xs bg-muted/30 border-border"
          />
        </div>

        {/* Filters group */}
        <div className="flex items-center gap-2 flex-wrap">
          {/* Severity */}
          <Select
            value={severityFilter}
            onValueChange={(v) => {
              if (v) setSeverityFilter(v)
              setPage(0)
            }}
          >
            <SelectTrigger className="w-[130px] h-9 text-xs bg-muted/30 border-border">
              <Filter className="w-3 h-3 mr-1 text-muted-foreground" />
              <SelectValue placeholder="Severity" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Severities</SelectItem>
              <SelectItem value="CRITICAL">Critical</SelectItem>
              <SelectItem value="HIGH">High</SelectItem>
              <SelectItem value="MEDIUM">Medium</SelectItem>
              <SelectItem value="LOW">Low</SelectItem>
            </SelectContent>
          </Select>

          {/* Status */}
          <Select
            value={statusFilter}
            onValueChange={(v) => {
              if (v) setStatusFilter(v)
              setPage(0)
            }}
          >
            <SelectTrigger className="w-[140px] h-9 text-xs bg-muted/30 border-border">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Statuses</SelectItem>
              <SelectItem value="OPEN">Open</SelectItem>
              <SelectItem value="INVESTIGATING">Investigating</SelectItem>
              <SelectItem value="RESOLVED_AUTO">Resolved (Auto)</SelectItem>
              <SelectItem value="RESOLVED_MANUAL">Resolved (Manual)</SelectItem>
              <SelectItem value="ESCALATED">Escalated</SelectItem>
            </SelectContent>
          </Select>

          {/* Type */}
          <Select
            value={typeFilter}
            onValueChange={(v) => {
              if (v) setTypeFilter(v)
              setPage(0)
            }}
          >
            <SelectTrigger className="w-[160px] h-9 text-xs bg-muted/30 border-border">
              <SelectValue placeholder="Exception Type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Types</SelectItem>
              <SelectItem value="AMOUNT_MISMATCH">Amount Mismatch</SelectItem>
              <SelectItem value="MISSING_SETTLEMENT">Missing Settlement</SelectItem>
              <SelectItem value="UNEXPECTED_FEE">Unexpected Fee</SelectItem>
              <SelectItem value="DISCREPANT_REFUND">Discrepant Refund</SelectItem>
              <SelectItem value="DUPLICATE_TRANSACTION">Duplicate Payment</SelectItem>
            </SelectContent>
          </Select>

          {/* Reset Filters */}
          {(searchTerm || severityFilter !== "ALL" || statusFilter !== "ALL" || typeFilter !== "ALL") && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setSearchTerm("")
                setSeverityFilter("ALL")
                setStatusFilter("ALL")
                setTypeFilter("ALL")
                setPage(0)
              }}
              className="text-xs text-muted-foreground hover:text-foreground h-9"
            >
              Reset Filters
            </Button>
          )}
        </div>
      </div>

      {/* Exceptions Data Table */}
      <SectionCard noPadding>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border bg-muted/30 text-muted-foreground font-medium">
                <th className="py-3 px-4">Exception ID</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Expected</th>
                <th className="py-3 px-4">Actual</th>
                <th className="py-3 px-4">Discrepancy</th>
                <th className="py-3 px-4">Detected At</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {paginatedExceptions.length > 0 ? (
                paginatedExceptions.map((exp) => (
                  <tr
                    key={exp.id || exp.exceptionId}
                    className="hover:bg-muted/40 transition-colors cursor-pointer"
                    onClick={() => navigate(`/exceptions/${exp.exceptionId}`)}
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
                    <td className="py-3.5 px-4 font-mono text-zinc-300">
                      {formatCurrency(exp.expectedAmount)}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-zinc-300">
                      {formatCurrency(exp.actualAmount)}
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
                  <td colSpan={9} className="py-12">
                    <EmptyState
                      icon={<AlertTriangle className="w-6 h-6" />}
                      title="No exceptions found"
                      description="No financial exceptions match your current filter criteria."
                    />
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Table Footer Pagination */}
        <div className="p-4 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
          <div>
            Showing{" "}
            <span className="font-semibold text-foreground">
              {filteredExceptions.length > 0 ? page * pageSize + 1 : 0}
            </span>{" "}
            to{" "}
            <span className="font-semibold text-foreground">
              {Math.min((page + 1) * pageSize, filteredExceptions.length)}
            </span>{" "}
            of <span className="font-semibold text-foreground">{filteredExceptions.length}</span>{" "}
            exceptions
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
            <span className="px-2">
              Page {page + 1} of {totalPages}
            </span>
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
