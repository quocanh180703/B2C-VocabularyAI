import { apiClient } from "./apiClient"

export interface AiConversationListItem {
  id: string
  userId: string
  userEmail: string
  conversationType: string
  title: string
  messageCount: number
  isArchived: boolean
  createdAt: string
  updatedAt: string
}

export interface AiConversationDetail {
  id: string
  userId: string
  userEmail: string
  userFirstName: string
  userLastName: string
  conversationType: string
  title: string
  summary: string
  messageCount: number
  isArchived: boolean
  createdAt: string
  updatedAt: string
}

export interface AiStats {
  totalConversations: number
  activeUsers: number
  totalMessages: number
  avgMessagesPerConversation: number
  conversationTypeDistribution: {
    [key: string]: number
  }
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
 * AI Service - Fetch AI conversation data from backend APIs
 */
export const aiService = {
  /**
   * Get paginated list of AI conversations
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "createdAt,desc",
    userId?: string,
    type?: string,
    search?: string
  ): Promise<PageResponse<AiConversationListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (userId && userId.trim()) {
      params.append("userId", userId)
    }
    if (type && type !== "All") {
      params.append("type", type)
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<AiConversationListItem>>>(
      `/ai?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get AI conversation detail
   */
  async getDetail(conversationId: string): Promise<AiConversationDetail> {
    const response = await apiClient.get<ApiResponse<AiConversationDetail>>(
      `/ai/${conversationId}`
    )
    return response.data.data
  },

  /**
   * Get AI statistics
   */
  async getStats(): Promise<AiStats> {
    const response = await apiClient.get<ApiResponse<AiStats>>(
      `/ai/stats/summary`
    )
    return response.data.data
  },
}
