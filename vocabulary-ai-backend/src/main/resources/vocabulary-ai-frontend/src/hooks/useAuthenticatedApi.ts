import { useState, useCallback, useEffect } from 'react';
import { AxiosRequestConfig } from 'axios';
import { apiClient } from '@/services/api';
import { errorHandler, AppError } from '@/services/errorHandler';

/**
 * Custom Hook: useAuthenticatedApi
 * 
 * Hook để fetch dữ liệu từ API có xác thực
 * Tự động xử lý loading, error, và retry logic
 * 
 * Sử dụng:
 * const { data, isLoading, error, refetch } = useAuthenticatedApi('/vocabulary');
 */

export interface UseAuthenticatedApiState<T> {
  data: T | null;
  isLoading: boolean;
  error: AppError | null;
  isError: boolean;
}

interface UseAuthenticatedApiOptions extends AxiosRequestConfig {
  skip?: boolean;
  retry?: number;
  retryDelay?: number;
}

export function useAuthenticatedApi<T = any>(
  url: string,
  options: UseAuthenticatedApiOptions = {}
) {
  const [state, setState] = useState<UseAuthenticatedApiState<T>>({
    data: null,
    isLoading: true,
    error: null,
    isError: false,
  });

  const [retryCount, setRetryCount] = useState(0);

  const { skip = false, retry = 3, retryDelay = 1000, ...requestConfig } = options;

  /**
   * Fetch dữ liệu
   */
  const fetchData = useCallback(async () => {
    if (skip) {
      setState((prev) => ({ ...prev, isLoading: false }));
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, error: null, isError: false }));

    try {
      const response = await apiClient.get<T>(url, requestConfig);
      const data = response.data.data || response.data;

      setState({
        data,
        isLoading: false,
        error: null,
        isError: false,
      });
      setRetryCount(0);
    } catch (error: any) {
      const appError = errorHandler.handleApiError(error);

      // Retry logic
      if (retryCount < retry) {
        setTimeout(() => {
          setRetryCount((prev) => prev + 1);
        }, retryDelay);
      } else {
        setState({
          data: null,
          isLoading: false,
          error: appError,
          isError: true,
        });
      }
    }
  }, [url, skip, retry, retryDelay, retryCount, requestConfig]);

  /**
   * Refetch dữ liệu
   */
  const refetch = useCallback(() => {
    setRetryCount(0);
    fetchData();
  }, [fetchData]);

  /**
   * Fetch trên mount và khi dependency thay đổi
   */
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  /**
   * Retry khi retryCount thay đổi
   */
  useEffect(() => {
    if (retryCount > 0 && retryCount <= retry) {
      const timer = setTimeout(() => {
        fetchData();
      }, retryDelay);
      return () => clearTimeout(timer);
    }
  }, [retryCount, retry, retryDelay, fetchData]);

  return {
    ...state,
    refetch,
    retry: () => setRetryCount(0),
  };
}

/**
 * Custom Hook: useAuthenticatedApiMutation
 * 
 * Hook để POST/PUT/PATCH/DELETE requests
 * 
 * Sử dụng:
 * const { execute, isLoading, error, data } = useAuthenticatedApiMutation('/vocabulary', 'POST');
 * 
 * const handleCreate = async () => {
 *   await execute({ word: 'hello', meaning: 'greeting' });
 * };
 */

export interface UseAuthenticatedApiMutationState<T> {
  data: T | null;
  isLoading: boolean;
  error: AppError | null;
  isError: boolean;
  isSuccess: boolean;
}

type HttpMethod = 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export function useAuthenticatedApiMutation<T = any>(
  url: string,
  method: HttpMethod = 'POST',
  options: AxiosRequestConfig = {}
) {
  const [state, setState] = useState<UseAuthenticatedApiMutationState<T>>({
    data: null,
    isLoading: false,
    error: null,
    isError: false,
    isSuccess: false,
  });

  /**
   * Execute mutation
   */
  const execute = useCallback(
    async (data?: any, customUrl?: string) => {
      setState((prev) => ({
        ...prev,
        isLoading: true,
        error: null,
        isError: false,
        isSuccess: false,
      }));

      try {
        let response;
        const finalUrl = customUrl || url;

        switch (method) {
          case 'POST':
            response = await apiClient.post<T>(finalUrl, data, options);
            break;
          case 'PUT':
            response = await apiClient.put<T>(finalUrl, data, options);
            break;
          case 'PATCH':
            response = await apiClient.patch<T>(finalUrl, data, options);
            break;
          case 'DELETE':
            response = await apiClient.delete<T>(finalUrl, options);
            break;
          default:
            throw new Error(`Unsupported HTTP method: ${method}`);
        }

        const result = response.data.data || response.data;

        setState({
          data: result,
          isLoading: false,
          error: null,
          isError: false,
          isSuccess: true,
        });

        return result;
      } catch (error: any) {
        const appError = errorHandler.handleApiError(error);

        setState({
          data: null,
          isLoading: false,
          error: appError,
          isError: true,
          isSuccess: false,
        });

        throw appError;
      }
    },
    [method, options, url]
  );

  /**
   * Reset state
   */
  const reset = useCallback(() => {
    setState({
      data: null,
      isLoading: false,
      error: null,
      isError: false,
      isSuccess: false,
    });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
}
