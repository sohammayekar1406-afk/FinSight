import { useQuery } from "@tanstack/react-query"
import { auditApi } from "@/api/auditApi"

export function useAuditLogsPaged(page = 0, size = 20) {
  return useQuery({
    queryKey: ["audit-logs", "paged", page, size],
    queryFn: () => auditApi.getPaged(page, size),
  })
}
