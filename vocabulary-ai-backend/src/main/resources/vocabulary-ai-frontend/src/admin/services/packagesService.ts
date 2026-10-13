import { apiClient } from "./apiClient"

export interface PackageListItem {
  id: string
  code: string
  name: string
  price: number
  durationDays: number
  status: string
  totalPurchases: number
  revenue: number
  description: string
  createdAt: string
  updatedAt: string
}

export interface PackageDetail {
  id: string
  code: string
  name: string
  price: number
  durationDays: number
  status: string
  description: string
  totalPurchases: number
  revenue: number
  activeSubscriptions: number
  createdAt: string
  updatedAt: string
}

export interface PackageStats {
  totalPackages: number
  activePackages: number
  totalRevenue: number
  totalPurchases: number
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
 * Packages Service - Fetch package data from backend APIs
 */
export const packagesService = {
  /**
   * Get paginated list of packages
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "createdAt,desc",
    status?: string,
    search?: string
  ): Promise<PageResponse<PackageListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (status && status !== "All") {
      params.append("status", status)
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<PackageListItem>>>(
      `/packages?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get package detail
   */
  async getDetail(packageId: string): Promise<PackageDetail> {
    const response = await apiClient.get<ApiResponse<PackageDetail>>(
      `/packages/${packageId}`
    )
    return response.data.data
  },

  /**
   * Get package statistics
   */
  async getStats(): Promise<PackageStats> {
    const response = await apiClient.get<ApiResponse<PackageStats>>(
      `/packages/stats/summary`
    )
    return response.data.data
  },
}
