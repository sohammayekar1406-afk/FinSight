import { apiClient } from "./client"
import type { DashboardStats, HealthResponse } from "@/types/api"

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const { data } = await apiClient.get<DashboardStats>("/api/dashboard/stats")
    return data
  },

  getHealth: async (): Promise<HealthResponse> => {
    const { data } = await apiClient.get<HealthResponse>("/api/health")
    return data
  },
}
