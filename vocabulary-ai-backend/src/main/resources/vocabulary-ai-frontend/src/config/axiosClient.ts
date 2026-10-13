/**
 * Axios Client Configuration
 * 
 * Centralized HTTP client with:
 * - Base URL configuration
 * - JWT token interceptor
 * - Token refresh on 401
 * - Error handling
 */

import axios from 'axios';
import { env } from './environment';

/**
 * Create axios instance with default config
 */
const axiosClient = axios.create({
  baseURL: env.API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ============================================
// REQUEST INTERCEPTOR
// ============================================

/**
 * Attach JWT token to requests
 */
axiosClient.interceptors.request.use(
  (config) => {
    // Check both old and new token keys for backward compatibility
    const token = localStorage.getItem('vocabularyai_token') || localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.debug('✓ [REQUEST] JWT token attached', { 
        endpoint: config.url, 
        method: config.method,
        tokenExists: true 
      });
    } else {
      console.warn('✗ [REQUEST] No JWT token found in localStorage', { endpoint: config.url });
    }
    return config;
  },
  (error) => {
    console.error('❌ [REQUEST INTERCEPTOR ERROR]', error);
    return Promise.reject(error);
  }
);

// ============================================
// RESPONSE INTERCEPTOR
// ============================================

/**
 * Handle token expiration and refresh
 */
axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized (token expired or invalid)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Attempt token refresh
        const refreshToken = localStorage.getItem('vocabularyai_refresh_token') || localStorage.getItem('refresh_token');
        if (!refreshToken) {
          // No refresh token available, redirect to login
          localStorage.removeItem('vocabularyai_token');
          localStorage.removeItem('vocabularyai_refresh_token');
          localStorage.removeItem('access_token');
          localStorage.removeItem('refresh_token');
          window.location.href = '/login';
          return Promise.reject(error);
        }

        // Call refresh endpoint
        const response = await axios.post(
          `${env.API_BASE_URL || 'http://localhost:8080/api'}/auth/refresh`,
          { refreshToken }
        );

        const { accessToken } = response.data.data;
        localStorage.setItem('vocabularyai_token', accessToken);
        localStorage.setItem('access_token', accessToken);  // Backward compatibility

        // Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('vocabularyai_token');
        localStorage.removeItem('vocabularyai_refresh_token');
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    return Promise.reject(error);
  }
);

export default axiosClient;
