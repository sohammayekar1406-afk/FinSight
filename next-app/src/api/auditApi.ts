import { apiClient } from "./client"
import type { AuditLog, PagedResponse } from "@/types/api"

export const auditApi = {
  getPaged: async (page = 0, size = 20): Promise<PagedResponse<AuditLog>> => {
    const res = await apiClient.get<unknown>(
      `/api/audit-logs?page=${page}&size=${size}`
    )
    const raw = res?.data
    if (raw && typeof raw === "object") {
      const candidate = (raw as Record<string, unknown>).data ?? raw
      if (candidate && typeof candidate === "object" && Array.isArray((candidate as Record<string, unknown>).content)) {
        const c = candidate as PagedResponse<AuditLog>
        return {
          content: Array.isArray(c.content) ? c.content : [],
          page: c.page ?? page,
          size: c.size ?? size,
          totalElements: c.totalElements ?? (c.content?.length || 0),
          totalPages: c.totalPages ?? 1,
        }
      }
      if (Array.isArray(candidate)) {
        return {
          content: candidate as AuditLog[],
          page,
          size,
          totalElements: (candidate as AuditLog[]).length,
          totalPages: Math.ceil((candidate as AuditLog[]).length / size) || 1,
        }
      }
    }
    if (Array.isArray(raw)) {
      return {
        content: raw as AuditLog[],
        page,
        size,
        totalElements: (raw as AuditLog[]).length,
        totalPages: Math.ceil((raw as AuditLog[]).length / size) || 1,
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
}
