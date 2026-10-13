/**
 * API Client
 * Axios instance được cấu hình cho VocabularyAI Backend
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { env } from "@/config/environment";
import { ApiResponse, ApiError } from "./types";

class ApiClient {
  private instance: AxiosInstance;
  private requestInterceptorId?: number;
  private responseInterceptorId?: number;

  constructor() {
    // Tạo axios instance với base config
    this.instance = axios.create({
      baseURL: env.apiBaseUrl,
      timeout: env.apiTimeout,
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    this.setupInterceptors();
  }

  /**
   * Setup request/response interceptors
   */
  private setupInterceptors(): void {
    // Request interceptor - thêm token vào header
    this.requestInterceptorId = this.instance.interceptors.request.use(
      (config: any) => {
        const token = this.getAccessToken();
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Log request (chỉ trong development)
        if (env.debugMode) {
          console.log(`📤 [${config.method?.toUpperCase()}] ${config.url}`);
        }

        return config;
      },
      (error: any) => {
        if (env.debugMode) {
          console.error("❌ Request Error:", error);
        }
        return Promise.reject(error);
      }
    );

    // Response interceptor - xử lý response
    this.responseInterceptorId = this.instance.interceptors.response.use(
      (response: any) => {
        if (env.debugMode) {
          console.log(`✅ [${response.status}] ${response.config.url}`);
        }
        return response;
      },
      async (error: any) => {
        const { response } = error;

        if (env.debugMode) {
          console.error(`❌ [${response?.status}] ${response?.config.url}`, response?.data);
        }

        // Handle 401 - Token expired
        if (response?.status === 401) {
          const refreshed = await this.refreshAccessToken();
          if (refreshed) {
            // Retry request
            return this.instance(error.config);
          } else {
            // Logout
            this.handleUnauthorized();
          }
        }

        // Handle 403 - Forbidden
        if (response?.status === 403) {
          this.handleForbidden();
        }

        // Handle 404 - Not found
        if (response?.status === 404) {
          console.warn("❌ Resource not found:", error.config.url);
        }

        // Handle server errors
        if (response?.status >= 500) {
          console.error("❌ Server error:", response.status);
          this.handleServerError(response.status);
        }

        return Promise.reject(error);
      }
    );
  }

  /**
   * Get access token from localStorage
   */
  private getAccessToken(): string | null {
    return localStorage.getItem(env.tokenKey);
  }

  /**
   * Get refresh token from localStorage
   */
  private getRefreshToken(): string | null {
    return localStorage.getItem(env.refreshTokenKey);
  }

  /**
   * Save tokens to localStorage
   */
  private saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(env.tokenKey, accessToken);
    localStorage.setItem(env.refreshTokenKey, refreshToken);
  }

  /**
   * Clear tokens from localStorage
   */
  private clearTokens(): void {
    localStorage.removeItem(env.tokenKey);
    localStorage.removeItem(env.refreshTokenKey);
  }

  /**
   * Refresh access token using refresh token
   */
  private async refreshAccessToken(): Promise<boolean> {
    try {
      const refreshToken = this.getRefreshToken();
      if (!refreshToken) {
        return false;
      }

      const response = await this.instance.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
        "/auth/refresh",
        { refreshToken }
      );

      if (response.data.success && response.data.data) {
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;
        this.saveTokens(accessToken, newRefreshToken);
        return true;
      }

      return false;
    } catch (error) {
      console.error("Failed to refresh token:", error);
      return false;
    }
  }

  /**
   * Handle unauthorized errors (401)
   */
  private handleUnauthorized(): void {
    this.clearTokens();
    // Trigger logout event - sẽ được xử lý bởi auth service
    window.dispatchEvent(new CustomEvent("auth:unauthorized"));
  }

  /**
   * Handle forbidden errors (403)
   */
  private handleForbidden(): void {
    window.dispatchEvent(new CustomEvent("auth:forbidden"));
  }

  /**
   * Handle server errors (5xx)
   */
  private handleServerError(status: number): void {
    window.dispatchEvent(new CustomEvent("api:server-error", { detail: { status } }));
  }

  /**
   * GET request
   */
  async get<T = any>(
    url: string,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    return this.instance.get<ApiResponse<T>>(url, config);
  }

  /**
   * POST request
   */
  async post<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    return this.instance.post<ApiResponse<T>>(url, data, config);
  }

  /**
   * PUT request
   */
  async put<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    return this.instance.put<ApiResponse<T>>(url, data, config);
  }

  /**
   * PATCH request
   */
  async patch<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    return this.instance.patch<ApiResponse<T>>(url, data, config);
  }

  /**
   * DELETE request
   */
  async delete<T = any>(
    url: string,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    return this.instance.delete<ApiResponse<T>>(url, config);
  }

  /**
   * Upload file
   */
  async uploadFile<T = any>(
    url: string,
    file: File,
    additionalData?: Record<string, any>,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<ApiResponse<T>>> {
    const formData = new FormData();
    formData.append("file", file);

    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, value);
      });
    }

    return this.post<T>(url, formData, {
      ...config,
      headers: {
        ...config?.headers,
        "Content-Type": "multipart/form-data",
      },
    });
  }

  /**
   * Get axios instance (để custom config khi cần)
   */
  getInstance(): AxiosInstance {
    return this.instance;
  }

  /**
   * Cleanup interceptors
   */
  destroy(): void {
    if (this.requestInterceptorId !== undefined) {
      this.instance.interceptors.request.eject(this.requestInterceptorId);
    }
    if (this.responseInterceptorId !== undefined) {
      this.instance.interceptors.response.eject(this.responseInterceptorId);
    }
  }
}

// Singleton instance
export const apiClient = new ApiClient();

export default apiClient;
