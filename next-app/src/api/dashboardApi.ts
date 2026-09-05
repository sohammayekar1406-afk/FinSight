import { apiClient } from "./client"
import type { DashboardStats, HealthResponse } from "@/types/api"

const DEFAULT_STATS: DashboardStats = {
  totalTransactions: 0,
  successfulPayments: 0,
  refundsCount: 0,
  totalRefundsAmount: 0,
  totalFeesAmount: 0,
  totalSettlements: 0,
  totalSettlementsAmount: 0,
  unreconciledAmount: 0,
  openExceptionsCount: 0,
  severityBreakdown: { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0 },
  settlementOverview: { SETTLED: 0, DISCREPANT: 0, PENDING: 0 },
  recentExceptions: [],
  hasReconciled: false,
  lastReconciledAt: null,
}

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const res = await apiClient.get<unknown>("/api/dashboard/stats")
    const raw = res?.data
    let stats: Record<string, unknown> | null = null
    if (raw && typeof raw === "object") {
      if ((raw as Record<string, unknown>).data && typeof (raw as Record<string, unknown>).data === "object") {
        stats = (raw as Record<string, unknown>).data as Record<string, unknown>
      } else {
        stats = raw as Record<string, unknown>
      }
    }
    if (!stats) return DEFAULT_STATS

    return {
      totalTransactions: Number(stats.totalTransactions) || 0,
      successfulPayments: Number(stats.successfulPayments) || 0,
      refundsCount: Number(stats.refundsCount) || 0,
      totalRefundsAmount: Number(stats.totalRefundsAmount) || 0,
      totalFeesAmount: Number(stats.totalFeesAmount) || 0,
      totalSettlements: Number(stats.totalSettlements) || 0,
      totalSettlementsAmount: Number(stats.totalSettlementsAmount) || 0,
      unreconciledAmount: Number(stats.unreconciledAmount) || 0,
      openExceptionsCount: Number(stats.openExceptionsCount) || 0,
      severityBreakdown: (stats.severityBreakdown as Record<string, number>) || { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0 },
      settlementOverview: (stats.settlementOverview as Record<string, number>) || { SETTLED: 0, DISCREPANT: 0, PENDING: 0 },
      recentExceptions: Array.isArray(stats.recentExceptions) ? (stats.recentExceptions as DashboardStats["recentExceptions"]) : [],
      hasReconciled: Boolean(stats.hasReconciled),
      lastReconciledAt: (stats.lastReconciledAt as string) || null,
    }
  },

  getHealth: async (): Promise<HealthResponse> => {
    const res = await apiClient.get<unknown>("/api/health")
    const raw = res?.data
    let health = raw as HealthResponse
    if (raw && typeof raw === "object" && (raw as Record<string, unknown>).data) {
      health = (raw as Record<string, unknown>).data as HealthResponse
    }
    return health
  },
}
