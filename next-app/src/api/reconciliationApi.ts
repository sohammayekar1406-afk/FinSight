import { apiClient } from "./client"
import type { ReconciliationResult } from "@/types/api"

export const reconciliationApi = {
  run: async (): Promise<ReconciliationResult> => {
    const { data } = await apiClient.post<ReconciliationResult>("/api/reconciliation/run")
    return data
  },
}
