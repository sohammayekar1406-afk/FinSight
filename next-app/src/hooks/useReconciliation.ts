import { useMutation, useQueryClient } from "@tanstack/react-query"
import { reconciliationApi } from "@/api/reconciliationApi"

// Note: the backend has no GET /api/reconciliation/status endpoint.
// Status is derived from the POST /api/reconciliation/run response.

export function useRunReconciliation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: reconciliationApi.run,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["dashboard"] })
      queryClient.invalidateQueries({ queryKey: ["exceptions"] })
      queryClient.invalidateQueries({ queryKey: ["audit-logs"] })
    },
  })
}
