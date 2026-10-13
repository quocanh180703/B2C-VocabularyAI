import { useState, useEffect } from 'react';
import * as dashboardService from '@/services/dashboardService';
import { useAuth } from '@/hooks/useAuth';

export interface DashboardState {
  data: dashboardService.DashboardData | null;
  loading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
}

/**
 * useDashboard Hook
 * 
 * Fetches all dashboard data from backend APIs
 * Handles loading, error, and refetch states
 * Verifies authentication before fetching
 * 
 * Usage:
 * ```
 * const { data, loading, error, refetch } = useDashboard();
 * if (loading) return <Skeleton />;
 * if (error) return <ErrorState onRetry={refetch} />;
 * return <Dashboard data={data} />;
 * ```
 */
export function useDashboard(): DashboardState {
  const [data, setData] = useState<dashboardService.DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const { isAuthenticated, user } = useAuth();

  const fetchDashboardData = async () => {
    try {
      // Verify authentication before fetching
      if (!isAuthenticated) {
        throw new Error('User is not authenticated. Please log in again.');
      }

      if (!user) {
        throw new Error('User profile not loaded. Please refresh the page.');
      }

      setLoading(true);
      setError(null);

      console.log(`📊 Fetching dashboard data for user: ${user.id}`, {
        username: user.username,
        email: user.email,
        timestamp: new Date().toISOString(),
      });

      const dashboardData = await dashboardService.getDashboardData();
      
      // Verify data belongs to authenticated user
      if (dashboardData.user.id !== user.id) {
        throw new Error('Data mismatch: Received data for different user');
      }

      console.log(`✅ Dashboard data loaded successfully for user: ${user.id}`);
      setData(dashboardData);
    } catch (err: any) {
      let errorMessage = 'Failed to load dashboard';
      
      if (err.response?.status === 403) {
        errorMessage = 'Access denied. Please log in again.';
      } else if (err.response?.status === 401) {
        errorMessage = 'Your session has expired. Please log in again.';
      } else if (err.response?.status === 404) {
        errorMessage = 'API endpoint not found. Please check backend.';
      } else if (err.response?.status >= 500) {
        errorMessage = 'Server error. Please try again later.';
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }
      
      const dashboardError = new Error(errorMessage);
      setError(dashboardError);
      console.error('Dashboard fetch error:', {
        userId: user?.id,
        error: err,
        status: err.response?.status,
        errorMessage,
        data: err.response?.data,
        timestamp: new Date().toISOString(),
      });
    } finally {
      setLoading(false);
    }
  };

  // Fetch data on component mount or when authentication changes
  useEffect(() => {
    if (isAuthenticated && user) {
      fetchDashboardData();
    }
  }, [isAuthenticated, user?.id]);

  const refetch = async () => {
    await fetchDashboardData();
  };

  return {
    data,
    loading,
    error,
    refetch,
  };
}
