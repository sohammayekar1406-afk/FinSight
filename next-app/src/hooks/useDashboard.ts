import { useQuery } from "@tanstack/react-query"
import { dashboardApi } from "@/api/dashboardApi"

export function useDashboardStats() {
  return useQuery({
    queryKey: ["dashboard", "stats"],
    queryFn: dashboardApi.getStats,
    refetchInterval: 30_000, // auto-refresh every 30 seconds
  })
}

export function useHealth() {
  return useQuery({
    queryKey: ["health"],
    queryFn: dashboardApi.getHealth,
    staleTime: 60_000,
  })
}
