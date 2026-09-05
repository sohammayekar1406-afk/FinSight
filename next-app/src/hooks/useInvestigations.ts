import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { investigationsApi } from "@/api/investigationsApi"

export function useInvestigationDetail(exceptionId: string | undefined) {
  return useQuery({
    queryKey: ["investigations", "detail", exceptionId],
    queryFn: () => investigationsApi.get(exceptionId!),
    enabled: !!exceptionId,
    retry: false, // investigation might not exist yet
    staleTime: 0,
  })
}

export function useRunInvestigation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (exceptionId: string) => investigationsApi.investigate(exceptionId),
    onSuccess: (_, exceptionId) => {
      queryClient.invalidateQueries({ queryKey: ["investigations", "detail", exceptionId] })
      queryClient.invalidateQueries({ queryKey: ["exceptions"] })
      queryClient.invalidateQueries({ queryKey: ["dashboard"] })
    },
  })
}

export function useRunAllInvestigations() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: investigationsApi.investigateAll,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["investigations"] })
      queryClient.invalidateQueries({ queryKey: ["exceptions"] })
      queryClient.invalidateQueries({ queryKey: ["dashboard"] })
    },
  })
}

export function useResolveException() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (exceptionId: string) => investigationsApi.resolve(exceptionId),
    onSuccess: (_, exceptionId) => {
      queryClient.invalidateQueries({ queryKey: ["investigations", "detail", exceptionId] })
      queryClient.invalidateQueries({ queryKey: ["exceptions"] })
      queryClient.invalidateQueries({ queryKey: ["dashboard"] })
      queryClient.invalidateQueries({ queryKey: ["audit-logs"] })
    },
  })
}
