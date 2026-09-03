import { apiClient } from "./client"
import type { AuditLog, PagedResponse } from "@/types/api"

export const auditApi = {
  getPaged: async (page = 0, size = 20): Promise<PagedResponse<AuditLog>> => {
    const { data } = await apiClient.get<PagedResponse<AuditLog>>(
      `/api/audit-logs?page=${page}&size=${size}`
    )
    return data
  },
}
