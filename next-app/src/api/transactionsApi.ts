import { apiClient } from "./client"

export interface TransactionItem {
  id: string
  orderId: string
  paymentId: string
  settlementId: string | null
  amount: number
  method: string
  status: string
  date: string
}

export const transactionsApi = {
  getAll: async (): Promise<TransactionItem[]> => {
    const res = await apiClient.get<unknown>("/api/payments")
    const raw = res?.data
    let list: unknown[] = []
    if (Array.isArray(raw)) {
      list = raw
    } else if (raw && typeof raw === "object") {
      if (Array.isArray((raw as Record<string, unknown>).data)) {
        list = (raw as { data: unknown[] }).data
      } else if (Array.isArray((raw as Record<string, unknown>).content)) {
        list = (raw as { content: unknown[] }).content
      }
    }

    return list.map((item, idx) => {
      const row = (item && typeof item === "object") ? (item as Record<string, unknown>) : {}
      return {
        id: (row.paymentId as string) || (row.id as string) || `tx_${idx + 1001}`,
        orderId: (row.orderId as string) || `ord_${idx + 1001}`,
        paymentId: (row.paymentId as string) || (row.id as string) || `pay_${idx + 1001}`,
        settlementId: (row.settlementId as string) || null,
        amount: Number(row.amount) || 0,
        method: typeof row.method === "string" ? row.method : "CARD",
        status: typeof row.status === "string" ? row.status : "SETTLED",
        date: (row.createdAt as string) || new Date().toISOString(),
      }
    })
  },
}
