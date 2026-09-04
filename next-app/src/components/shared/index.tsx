/* eslint-disable react-refresh/only-export-components */
import type { ReactNode } from "react"
import type { ExceptionSeverity, ExceptionStatus, ExceptionType } from "@/types/api"

// ============================================================
// SeverityBadge
// ============================================================

const SEVERITY_CONFIG: Record<
  ExceptionSeverity,
  { label: string; className: string }
> = {
  CRITICAL: { label: "Critical", className: "bg-rose-500/10 text-rose-400 border-rose-500/25" },
  HIGH: { label: "High", className: "bg-orange-500/10 text-orange-400 border-orange-500/25" },
  MEDIUM: { label: "Medium", className: "bg-amber-500/10 text-amber-400 border-amber-500/25" },
  LOW: { label: "Low", className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/25" },
}

export function SeverityBadge({ severity }: { severity: ExceptionSeverity }) {
  const config = SEVERITY_CONFIG[severity]
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-medium border ${config.className}`}
    >
      {config.label}
    </span>
  )
}

// ============================================================
// StatusBadge
// ============================================================

const STATUS_CONFIG: Record<ExceptionStatus, { label: string; className: string }> = {
  OPEN: { label: "Open", className: "bg-zinc-500/10 text-zinc-300 border-zinc-500/25" },
  INVESTIGATING: { label: "Investigating", className: "bg-purple-500/10 text-purple-300 border-purple-500/25" },
  RESOLVED_AUTO: { label: "Resolved (Auto)", className: "bg-emerald-500/10 text-emerald-400 border-emerald-500/25" },
  RESOLVED_MANUAL: { label: "Resolved (Manual)", className: "bg-teal-500/10 text-teal-400 border-teal-500/25" },
  ESCALATED: { label: "Escalated", className: "bg-rose-500/10 text-rose-400 border-rose-500/25" },
}

export function StatusBadge({ status }: { status: ExceptionStatus }) {
  const config = STATUS_CONFIG[status]
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-medium border ${config.className}`}
    >
      {config.label}
    </span>
  )
}

// ============================================================
// ExceptionTypeBadge
// ============================================================

const TYPE_LABELS: Record<ExceptionType, string> = {
  AMOUNT_MISMATCH: "Amount Mismatch",
  MISSING_PAYMENT: "Missing Payment",
  MISSING_SETTLEMENT: "Missing Settlement",
  UNEXPECTED_FEE: "Unexpected Fee",
  DUPLICATE_TRANSACTION: "Duplicate Transaction",
  DELAYED_SETTLEMENT: "Delayed Settlement",
  DISCREPANT_REFUND: "Discrepant Refund",
  UNKNOWN_TRANSACTION: "Unknown Transaction",
  UNMATCHED_ADJUSTMENT: "Unmatched Adjustment",
  CURRENCY_MISMATCH: "Currency Mismatch",
}

export function ExceptionTypeBadge({ type }: { type: ExceptionType }) {
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-medium bg-muted/60 text-zinc-300 border border-border/70 font-mono">
      {TYPE_LABELS[type]}
    </span>
  )
}

// ============================================================
// PageHeader
// ============================================================

interface PageHeaderProps {
  title: string
  description?: string
  actions?: ReactNode
  className?: string
}

export function PageHeader({ title, description, actions, className = "" }: PageHeaderProps) {
  return (
    <div className={`flex items-start justify-between gap-4 ${className}`}>
      <div>
        <h1 className="text-xl font-semibold text-foreground tracking-tight">{title}</h1>
        {description && (
          <p className="text-sm text-muted-foreground/60 mt-0.5">{description}</p>
        )}
      </div>
      {actions && <div className="flex items-center gap-2 flex-shrink-0">{actions}</div>}
    </div>
  )
}

// ============================================================
// SectionCard
// ============================================================

interface SectionCardProps {
  title?: string
  description?: string
  actions?: ReactNode
  children: ReactNode
  className?: string
  noPadding?: boolean
}

export function SectionCard({
  title,
  description,
  actions,
  children,
  className = "",
  noPadding = false,
}: SectionCardProps) {
  return (
    <div className={`ll-card ${className}`}>
      {/* Subtle top edge highlight */}
      <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent pointer-events-none" />
      {(title || description || actions) && (
        <div className="flex items-center justify-between gap-4 px-5 py-4 border-b border-border/80">
          <div>
            {title && (
              <h2 className="text-sm font-semibold text-foreground tracking-tight">{title}</h2>
            )}
            {description && (
              <p className="text-xs text-muted-foreground/70 mt-0.5">{description}</p>
            )}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}
      <div className={noPadding ? "" : "p-5"}>{children}</div>
    </div>
  )
}

// ============================================================
// MetricCard
// ============================================================

interface MetricCardProps {
  title: string
  value: string | number
  description?: ReactNode
  icon?: ReactNode
  trend?: {
    value: string
    positive: boolean
  }
  className?: string
  accentColor?: string
}

export function MetricCard({
  title,
  value,
  description,
  icon,
  trend,
  className = "",
  accentColor = "text-foreground",
}: MetricCardProps) {
  return (
    <div className={`ll-card relative overflow-hidden p-5 flex flex-col justify-between gap-3.5 transition-all duration-200 hover:border-zinc-700/80 hover:shadow-lg hover:shadow-black/25 hover:-translate-y-0.5 ${className}`}>
      {/* Subtle top edge highlight */}
      <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent pointer-events-none" />

      <div className="flex items-center justify-between gap-2">
        <span className="text-[11px] font-semibold text-muted-foreground/70 uppercase tracking-wider">
          {title}
        </span>
        {icon && (
          <div className="w-8 h-8 rounded-lg flex items-center justify-center bg-muted/40 border border-border/60 text-muted-foreground shrink-0">
            {icon}
          </div>
        )}
      </div>

      <div className="space-y-1">
        <div className="flex items-baseline justify-between gap-2">
          <div className={`text-3xl font-bold tracking-tighter ${accentColor}`}>
            {value}
          </div>
          {trend && (
            <span
              className={`text-xs font-medium px-2 py-0.5 rounded-full border ${
                trend.positive
                  ? "text-emerald-400 bg-emerald-500/10 border-emerald-500/20"
                  : "text-rose-400 bg-rose-500/10 border-rose-500/20"
              }`}
            >
              {trend.positive ? "↑" : "↓"} {trend.value}
            </span>
          )}
        </div>
        {description && (
          <div className="text-xs text-muted-foreground/70 leading-relaxed font-medium">
            {description}
          </div>
        )}
      </div>
    </div>
  )
}

// ============================================================
// EmptyState
// ============================================================

interface EmptyStateProps {
  icon?: ReactNode
  title: string
  description?: string
  action?: ReactNode
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
      {icon && (
        <div className="w-12 h-12 rounded-xl bg-muted/60 flex items-center justify-center text-muted-foreground">
          {icon}
        </div>
      )}
      <div>
        <p className="text-sm font-medium text-foreground">{title}</p>
        {description && (
          <p className="text-sm text-muted-foreground mt-1 max-w-sm">{description}</p>
        )}
      </div>
      {action && <div className="mt-1">{action}</div>}
    </div>
  )
}

// ============================================================
// LoadingState
// ============================================================

export function LoadingState({ message = "Loading..." }: { message?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16">
      <div className="w-6 h-6 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
      <p className="text-sm text-muted-foreground">{message}</p>
    </div>
  )
}

// ============================================================
// ErrorState
// ============================================================

interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
}

export function ErrorState({
  title = "Something went wrong",
  message,
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
      <div className="w-12 h-12 rounded-xl bg-destructive/10 flex items-center justify-center">
        <svg className="w-6 h-6 text-destructive" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
      </div>
      <div>
        <p className="text-sm font-medium text-foreground">{title}</p>
        {message && (
          <p className="text-sm text-muted-foreground mt-1 max-w-sm">{message}</p>
        )}
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-sm text-primary hover:underline font-medium mt-1"
        >
          Try again
        </button>
      )}
    </div>
  )
}

// ============================================================
// ChartCard
// ============================================================

interface ChartCardProps {
  title: string
  description?: string
  children: ReactNode
  className?: string
  actions?: ReactNode
}

export function ChartCard({ title, description, children, className = "", actions }: ChartCardProps) {
  return (
    <div className={`ll-card ${className}`}>
      <div className="flex items-center justify-between px-5 py-4 border-b border-border">
        <div>
          <h3 className="text-sm font-semibold text-foreground">{title}</h3>
          {description && <p className="text-xs text-muted-foreground mt-0.5">{description}</p>}
        </div>
        {actions && <div>{actions}</div>}
      </div>
      <div className="p-5">{children}</div>
    </div>
  )
}

// ============================================================
// SkeletonCard
// ============================================================

export function SkeletonCard({ lines = 3 }: { lines?: number }) {
  return (
    <div className="ll-card p-5 flex flex-col gap-3">
      <div className="skeleton h-3 w-24 rounded" />
      <div className="skeleton h-8 w-32 rounded" />
      {Array.from({ length: lines - 2 }).map((_, i) => (
        <div key={i} className="skeleton h-3 rounded" style={{ width: `${60 + i * 15}%` }} />
      ))}
    </div>
  )
}

// ============================================================
// TransactionLineage
// ============================================================

interface LineageNode {
  id: string
  label: string
  value?: string
  status?: "normal" | "exception" | "warning" | "resolved"
}

interface TransactionLineageProps {
  nodes: LineageNode[]
  compact?: boolean
}

export function TransactionLineage({ nodes, compact = false }: TransactionLineageProps) {
  const statusStyles = {
    normal: "bg-muted text-muted-foreground border-border",
    exception: "bg-destructive/10 text-destructive border-destructive/30",
    warning: "bg-yellow-500/10 text-yellow-500 border-yellow-500/30",
    resolved: "bg-emerald-500/10 text-emerald-500 border-emerald-500/30",
  }

  return (
    <div className={`flex ${compact ? "gap-2" : "gap-3"} items-center flex-wrap`}>
      {nodes.map((node, i) => (
        <div key={node.id} className="flex items-center gap-2">
          <div
            className={`flex flex-col gap-0.5 px-3 py-2 rounded-lg border text-xs ${statusStyles[node.status ?? "normal"]} ${compact ? "py-1 px-2" : ""}`}
          >
            <span className="font-medium">{node.label}</span>
            {node.value && !compact && (
              <span className="font-mono opacity-70">{node.value}</span>
            )}
          </div>
          {i < nodes.length - 1 && (
            <svg
              className="w-4 h-4 text-muted-foreground/40 flex-shrink-0"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5l7 7-7 7" />
            </svg>
          )}
        </div>
      ))}
    </div>
  )
}

// ============================================================
// Currency formatter
// ============================================================

export function formatCurrency(amount: number | null | undefined, currency = "INR"): string {
  if (amount === null || amount === undefined) return "—"
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

// ============================================================
// Date formatter
// ============================================================

export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return "—"
  return new Intl.DateTimeFormat("en-IN", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(dateString))
}

export function formatDateShort(dateString: string | null | undefined): string {
  if (!dateString) return "—"
  return new Intl.DateTimeFormat("en-IN", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(dateString))
}
