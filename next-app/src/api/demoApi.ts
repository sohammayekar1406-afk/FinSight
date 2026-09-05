import { apiClient } from "./client"
import type { SeedResponse, DemoValidationReport } from "@/types/api"

export const demoApi = {
  seed: async (): Promise<SeedResponse> => {
    const { data } = await apiClient.post<SeedResponse>("/api/demo/seed")
    return data
  },

  validate: async (): Promise<DemoValidationReport> => {
    const { data } = await apiClient.post<DemoValidationReport>("/api/demo/validate")
    return data
  },

  reset: async (): Promise<{ status: string; message: string }> => {
    const { data } = await apiClient.post<{ status: string; message: string }>("/api/demo/reset")
    return data
  },
}
