import { useQuery } from "@tanstack/react-query"
import { exceptionsApi } from "@/api/exceptionsApi"

export function useExceptions() {
  return useQuery({
    queryKey: ["exceptions", "all"],
    queryFn: async () => {
      const res = await exceptionsApi.getAll()
      return Array.isArray(res) ? res : []
    },
    initialData: [],
  })
}

export function useExceptionsPaged(page = 0, size = 20) {
  return useQuery({
    queryKey: ["exceptions", "paged", page, size],
    queryFn: () => exceptionsApi.getPaged(page, size),
  })
}

export function useExceptionDetail(exceptionId: string | undefined) {
  return useQuery({
    queryKey: ["exceptions", "detail", exceptionId],
    queryFn: () => exceptionsApi.getById(exceptionId!),
    enabled: !!exceptionId,
  })
}
