import { useState, useMemo } from "react"
import { useTransactions, type TransactionItem } from "@/hooks/useTransactions"
import {
  PageHeader,
  SectionCard,
  LoadingState,
  ErrorState,
  EmptyState,
  formatCurrency,
  formatDateShort,
} from "@/components/shared"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  ArrowLeftRight,
  Search,
  Filter,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  AlertTriangle,
} from "lucide-react"

export default function TransactionsPage() {
  const { data: fetchedTransactions, isLoading, isError, refetch } = useTransactions()

  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("ALL")
  const [page, setPage] = useState(0)
  const pageSize = 10

  const allTransactions: TransactionItem[] = useMemo(() => {
    return Array.isArray(fetchedTransactions) ? fetchedTransactions : []
  }, [fetchedTransactions])

  const filteredTransactions = useMemo(() => {
    return allTransactions.filter((tx) => {
      const matchesSearch =
        !searchTerm ||
        tx.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tx.orderId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tx.paymentId.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesStatus = statusFilter === "ALL" || tx.status === statusFilter

      return matchesSearch && matchesStatus
    })
  }, [allTransactions, searchTerm, statusFilter])

  const paginatedTransactions = useMemo(() => {
    const start = page * pageSize
    return filteredTransactions.slice(start, start + pageSize)
  }, [filteredTransactions, page, pageSize])

  const totalPages = Math.ceil(filteredTransactions.length / pageSize) || 1

  if (isLoading) return <LoadingState message="Loading financial transaction ledger..." />
  if (isError) return <ErrorState title="Unable to load transactions" onRetry={refetch} />

  return (
    <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8">
      {/* Top Header */}
      <PageHeader
        title="Transaction Ledger"
        description="Comprehensive audit view of merchant orders, payment gateway charges, and settlement batches."
        actions={
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="text-xs border-border/80 hover:bg-muted/60 hover:text-foreground transition-all duration-150"
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Refresh
          </Button>
        }
      />

      {/* Filter Controls Bar */}
      <div className="p-4 rounded-xl border border-border/80 bg-card shadow-sm flex flex-col md:flex-row items-stretch md:items-center gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search Transaction ID, Order ID, Payment ID..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value)
              setPage(0)
            }}
            className="pl-9 h-9 text-xs bg-muted/30 border-border/80 focus-visible:ring-1 focus-visible:ring-zinc-600"
          />
        </div>

        <div className="flex items-center gap-2">
          <Select
            value={statusFilter}
            onValueChange={(v) => {
              if (v) setStatusFilter(v)
              setPage(0)
            }}
          >
            <SelectTrigger className="w-[160px] h-9 text-xs bg-muted/30 border-border/80">
              <Filter className="w-3 h-3 mr-1 text-muted-foreground" />
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Statuses</SelectItem>
              <SelectItem value="SETTLED">Settled</SelectItem>
              <SelectItem value="DISCREPANCY">Discrepancy</SelectItem>
              <SelectItem value="UNSETTLED">Unsettled</SelectItem>
              <SelectItem value="PARTIAL_REFUND">Partial Refund</SelectItem>
            </SelectContent>
          </Select>

          {searchTerm || statusFilter !== "ALL" ? (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setSearchTerm("")
                setStatusFilter("ALL")
                setPage(0)
              }}
              className="text-xs text-muted-foreground hover:text-foreground h-9"
            >
              Reset
            </Button>
          ) : null}
        </div>
      </div>

      {/* Data Table */}
      <SectionCard noPadding>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border/80 bg-muted/40 text-muted-foreground/70 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Transaction ID</th>
                <th className="py-3 px-4">Order ID</th>
                <th className="py-3 px-4">Payment ID</th>
                <th className="py-3 px-4">Settlement ID</th>
                <th className="py-3 px-4">Method</th>
                <th className="py-3 px-4 text-right">Amount</th>
                <th className="py-3 px-4">Ledger Status</th>
                <th className="py-3 px-4">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {paginatedTransactions.length > 0 ? (
                paginatedTransactions.map((tx) => (
                  <tr key={tx.id} className="even:bg-muted/15 hover:bg-muted/35 transition-colors duration-150">
                    <td className="py-3.5 px-4 font-mono font-medium text-foreground">{tx.id}</td>
                    <td className="py-3.5 px-4 font-mono text-zinc-300">{tx.orderId}</td>
                    <td className="py-3.5 px-4 font-mono text-zinc-300">{tx.paymentId}</td>
                    <td className="py-3.5 px-4 font-mono text-zinc-400">{tx.settlementId || "—"}</td>
                    <td className="py-3.5 px-4">
                      <Badge variant="outline" className="text-[10px] bg-muted/50 border-border/70 text-zinc-300 font-mono">
                        {tx.method}
                      </Badge>
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono font-semibold text-foreground">
                      {formatCurrency(tx.amount)}
                    </td>
                    <td className="py-3.5 px-4">
                      {tx.status === "SETTLED" ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/25">
                          <CheckCircle2 className="w-3 h-3 text-emerald-400" /> SETTLED
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-amber-500/10 text-amber-400 border border-amber-500/25">
                          <AlertTriangle className="w-3 h-3 text-amber-400" /> {tx.status}
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-muted-foreground">{formatDateShort(tx.date)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} className="py-12">
                    <EmptyState
                      icon={<ArrowLeftRight className="w-6 h-6" />}
                      title="No transactions found"
                      description={
                        searchTerm || statusFilter !== "ALL"
                          ? "No records match the current filter selection."
                          : "No transactions recorded yet. Seed demo data in Settings to populate the transaction ledger."
                      }
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
            Showing{" "}
            <span className="font-semibold text-foreground">
              {filteredTransactions.length === 0 ? 0 : page * pageSize + 1}
            </span>{" "}
            to{" "}
            <span className="font-semibold text-foreground">
              {Math.min((page + 1) * pageSize, filteredTransactions.length)}
            </span>{" "}
            of{" "}
            <span className="font-semibold text-foreground">{filteredTransactions.length}</span>{" "}
            records
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
