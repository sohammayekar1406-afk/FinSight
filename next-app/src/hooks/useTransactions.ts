import { useQuery } from "@tanstack/react-query"
import { transactionsApi, type TransactionItem } from "@/api/transactionsApi"

export function useTransactions() {
  return useQuery<TransactionItem[]>({
    queryKey: ["transactions", "all"],
    queryFn: async () => {
      const res = await transactionsApi.getAll()
      return Array.isArray(res) ? res : []
    },
  })
}

export type { TransactionItem }
