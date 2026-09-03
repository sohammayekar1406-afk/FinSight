import { apiClient } from "./client"
import type { FinancialException, PagedResponse } from "@/types/api"

export const exceptionsApi = {
  getAll: async (): Promise<FinancialException[]> => {
    const res = await apiClient.get<unknown>("/api/exceptions")
    const raw = res?.data
    if (Array.isArray(raw)) return raw
    if (raw && typeof raw === "object") {
      if (Array.isArray((raw as Record<string, unknown>).data)) {
        return (raw as { data: FinancialException[] }).data
      }
      if (Array.isArray((raw as Record<string, unknown>).content)) {
        return (raw as { content: FinancialException[] }).content
      }
    }
    return []
  },

  getPaged: async (page = 0, size = 20): Promise<PagedResponse<FinancialException>> => {
    const res = await apiClient.get<unknown>(
      `/api/exceptions/paged?page=${page}&size=${size}`
    )
    const raw = res?.data
    if (raw && typeof raw === "object" && Array.isArray((raw as Record<string, unknown>).content)) {
      return raw as PagedResponse<FinancialException>
    }
    if (Array.isArray(raw)) {
      return {
        content: raw,
        page,
        size,
        totalElements: raw.length,
        totalPages: Math.ceil(raw.length / size) || 1,
      }
    }
    return {
      content: [],
      page: 0,
      size,
      totalElements: 0,
      totalPages: 1,
    }
  },

  getById: async (exceptionId: string): Promise<FinancialException> => {
    const { data } = await apiClient.get<FinancialException>(`/api/exceptions/${exceptionId}`)
    return data
  },
}
