/**
 * Global Error Handler
 * Xử lý lỗi từ API và events
 */

import { useAuthStore } from '@/store/authStore';

export interface AppError {
  code: string;
  message: string;
  status?: number;
  details?: any;
  timestamp: number;
}

class ErrorHandler {
  private errorListeners: Set<(error: AppError) => void> = new Set();

  /**
   * Xử lý lỗi từ API response
   */
  handleApiError(error: any): AppError {
    const appError: AppError = {
      code: 'UNKNOWN_ERROR',
      message: 'An unexpected error occurred',
      timestamp: Date.now(),
    };

    if (error.response) {
      // Backend response với error status
      appError.status = error.response.status;
      appError.details = error.response.data;

      // Xử lý theo status code
      switch (error.response.status) {
        case 400:
          appError.code = 'BAD_REQUEST';
          appError.message =
            error.response.data?.message ||
            'Invalid request. Please check your input.';
          break;

        case 401:
          appError.code = 'UNAUTHORIZED';
          appError.message = 'Your session has expired. Please log in again.';
          this.handleUnauthorized();
          break;

        case 403:
          appError.code = 'FORBIDDEN';
          appError.message = 'You do not have permission to access this resource.';
          this.handleForbidden();
          break;

        case 404:
          appError.code = 'NOT_FOUND';
          appError.message =
            error.response.data?.message || 'Resource not found.';
          break;

        case 409:
          appError.code = 'CONFLICT';
          appError.message =
            error.response.data?.message ||
            'Resource already exists or has been modified.';
          break;

        case 422:
          appError.code = 'VALIDATION_ERROR';
          appError.message =
            error.response.data?.message ||
            'Validation failed. Please check your input.';
          appError.details = error.response.data?.errors;
          break;

        case 429:
          appError.code = 'RATE_LIMITED';
          appError.message =
            'Too many requests. Please try again later.';
          break;

        case 500:
          appError.code = 'INTERNAL_SERVER_ERROR';
          appError.message =
            'Server error. Please try again later.';
          break;

        case 502:
        case 503:
        case 504:
          appError.code = 'SERVICE_UNAVAILABLE';
          appError.message =
            'Service is temporarily unavailable. Please try again later.';
          break;

        default:
          appError.code = `HTTP_${error.response.status}`;
          appError.message =
            error.response.data?.message ||
            `Error: ${error.response.status}`;
      }
    } else if (error.request) {
      // Request được gửi nhưng không nhận response
      appError.code = 'NETWORK_ERROR';
      appError.message =
        'Network error. Please check your internet connection.';
    } else {
      // Error trong setup request
      appError.code = 'REQUEST_ERROR';
      appError.message = error.message || 'Failed to set up request.';
    }

    this.notifyListeners(appError);
    return appError;
  }

  /**
   * Xử lý khi token hết hạn
   */
  private handleUnauthorized(): void {
    const authStore = useAuthStore.getState();
    authStore.logout();
  }

  /**
   * Xử lý khi không có quyền truy cập
   */
  private handleForbidden(): void {
    console.error('Access forbidden');
    // Có thể navigate đến forbidden page hoặc hiển thị notification
  }

  /**
   * Subscribe to error events
   */
  subscribe(listener: (error: AppError) => void): () => void {
    this.errorListeners.add(listener);
    return () => this.errorListeners.delete(listener);
  }

  /**
   * Notify all listeners
   */
  private notifyListeners(error: AppError): void {
    this.errorListeners.forEach((listener) => listener(error));
  }

  /**
   * Format error message cho user
   */
  getReadableErrorMessage(error: AppError): string {
    const messages: Record<string, string> = {
      UNAUTHORIZED: 'Your session has expired. Please log in again.',
      FORBIDDEN: 'You do not have permission for this action.',
      NOT_FOUND: 'The requested resource was not found.',
      VALIDATION_ERROR: 'Please check your input and try again.',
      RATE_LIMITED: 'Too many requests. Please wait before trying again.',
      NETWORK_ERROR: 'Network error. Please check your internet connection.',
      INTERNAL_SERVER_ERROR: 'Server error. Please try again later.',
      SERVICE_UNAVAILABLE: 'Service is temporarily unavailable.',
    };

    return messages[error.code] || error.message;
  }
}

// Singleton
export const errorHandler = new ErrorHandler();

// Listen to custom events
if (typeof window !== 'undefined') {
  window.addEventListener('auth:unauthorized', () => {
    errorHandler.handleApiError({
      response: {
        status: 401,
        data: { message: 'Unauthorized' },
      },
    });
  });

  window.addEventListener('auth:forbidden', () => {
    errorHandler.handleApiError({
      response: {
        status: 403,
        data: { message: 'Forbidden' },
      },
    });
  });

  window.addEventListener('api:server-error', (event: any) => {
    errorHandler.handleApiError({
      response: {
        status: event.detail?.status || 500,
        data: { message: 'Server error' },
      },
    });
  });
}

export default errorHandler;
