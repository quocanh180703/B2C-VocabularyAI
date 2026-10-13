import { useAppStore } from "../store/useAppStore"

export interface PackageFormData {
  code: string
  name: string
  price: number
  durationDays: number
  status: string
  description?: string
}

const API_BASE_URL = "/api/admin/packages"

/**
 * Package CRUD API service
 */
export const packageCrudService = {
  /**
   * Create new package
   */
  async create(data: PackageFormData): Promise<void> {
    const token = localStorage.getItem("token")
    const response = await fetch(`${API_BASE_URL}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to create package")
    }
  },

  /**
   * Update existing package
   */
  async update(id: number, data: Partial<PackageFormData>): Promise<void> {
    const token = localStorage.getItem("token")
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to update package")
    }
  },

  /**
   * Delete package
   */
  async delete(id: number): Promise<void> {
    const token = localStorage.getItem("token")
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to delete package")
    }
  },
}
