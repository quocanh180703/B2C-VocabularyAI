export interface SpeakingSessionFormData {
  topic: string
  level: string
  description?: string
  status: string
}

const API_BASE_URL = "/api/admin/speaking-session"

export const speakingSessionCrudService = {
  async create(data: SpeakingSessionFormData): Promise<void> {
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
      throw new Error(error.message || "Failed to create session")
    }
  },

  async update(id: number, data: Partial<SpeakingSessionFormData>): Promise<void> {
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
      throw new Error(error.message || "Failed to update session")
    }
  },

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
      throw new Error(error.message || "Failed to delete session")
    }
  },
}
