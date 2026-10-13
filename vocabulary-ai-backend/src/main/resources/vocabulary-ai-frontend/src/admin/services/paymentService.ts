import { apiClient } from "./apiClient"

export interface PaymentListItem {
  paymentId: number
  transactionCode: string
  orderCode: string
  userId: number
  userEmail: string
  userFullName: string
  packageName: string
  amount: number
  status: string
  paymentMethod: string
  paidAt: string
  createdAt: string
}

export interface PaymentDetail {
  paymentId: number
  transactionCode: string
  status: string
  amount: number
  paymentMethod: string
  paidAt: string
  createdAt: string
  orderId: number
  orderCode: string
  orderStatus: string
  orderExpiredAt: string
  userId: number
  username: string
  userEmail: string
  userFullName: string
  packageId: number
  packageName: string
  packageCode: string
  packagePrice: number
  packageDurationDays: number
  subscriptionId: number | null
  subscriptionStatus: string | null
  subscriptionStartAt: string | null
  subscriptionEndAt: string | null
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

/**
 * Payment Service - Fetch payment data from backend APIs
 */
export const paymentService = {
  /**
   * Get paginated list of payments
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "paidAt,desc",
    status?: string,
    userId?: number,
    search?: string
  ): Promise<PageResponse<PaymentListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (status && status !== "All") {
      params.append("status", status)
    }
    if (userId) {
      params.append("userId", userId.toString())
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<PaymentListItem>>>(
      `/payments?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get payment detail
   */
  async getDetail(paymentId: number): Promise<PaymentDetail> {
    const response = await apiClient.get<ApiResponse<PaymentDetail>>(
      `/payments/${paymentId}`
    )
    return response.data.data
  },
}
