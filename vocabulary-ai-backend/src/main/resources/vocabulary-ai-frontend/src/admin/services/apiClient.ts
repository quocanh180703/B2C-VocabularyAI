import axios from "axios"

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "/api/admin",
  timeout: 12_000,
  headers: { "Content-Type": "application/json" },
})

apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("access-token")
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
