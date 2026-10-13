import { apiClient } from "./apiClient"

export interface SubscriptionListItem {
  subscriptionId: number
  userId: number
  userEmail: string
  userFullName: string
  packageName: string
  startAt: string
  endAt: string
  amount: number
  status: string
  createdAt: string
}

export interface SubscriptionDetail {
  subscriptionId: number
  userId: number
  username: string
  userEmail: string
  userFullName: string
  packageId: number
  packageName: string
  packagePrice: number
  packageDurationDays: number
  status: string
  startAt: string
  endAt: string
  remainingDays: number
  autoRenew: boolean
  createdAt: string
  updatedAt: string
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
 * Subscription Service - Fetch subscription data from backend APIs
 */
export const subscriptionService = {
  /**
   * Get paginated list of subscriptions
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "startAt,desc",
    status?: string
  ): Promise<PageResponse<SubscriptionListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (status && status !== "All") {
      params.append("status", status)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<SubscriptionListItem>>>(
      `/subscriptions?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get active subscriptions (shorthand)
   */
  async listActive(
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SubscriptionListItem>> {
    return this.list(page, size, "startAt,desc", "ACTIVE")
  },

  /**
   * Get subscription detail
   */
  async getDetail(subscriptionId: number): Promise<SubscriptionDetail> {
    const response = await apiClient.get<ApiResponse<SubscriptionDetail>>(
      `/subscriptions/${subscriptionId}`
    )
    return response.data.data
  },
}
