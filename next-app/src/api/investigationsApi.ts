import { apiClient } from "./client"
import type { Investigation, RunInvestigationsResult } from "@/types/api"

export const investigationsApi = {
  get: async (exceptionId: string): Promise<Investigation> => {
    const { data } = await apiClient.get<Investigation>(`/api/investigations/${exceptionId}`)
    return data
  },

  investigate: async (exceptionId: string): Promise<Investigation> => {
    const { data } = await apiClient.post<Investigation>(`/api/investigations/${exceptionId}`)
    return data
  },

  investigateAll: async (): Promise<RunInvestigationsResult> => {
    const { data } = await apiClient.post<RunInvestigationsResult>("/api/investigations/run")
    return data
  },

  resolve: async (exceptionId: string): Promise<Investigation> => {
    const { data } = await apiClient.post<Investigation>(
      `/api/investigations/${exceptionId}/resolve`
    )
    return data
  },
}
