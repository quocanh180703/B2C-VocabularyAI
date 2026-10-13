import { ReactNode, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Auth Provider
 * 
 * Handles automatic token refresh
 * 
 * Backend token TTL: 1 hour (3600000 ms)
 * Refresh interval: 54 minutes (leave 6 minute buffer before expiration)
 */
export const AuthProvider = ({ children }: AuthProviderProps) => {
  const { token, refreshAccessToken, isExpired } = useAuth();

  useEffect(() => {
    // Skip if no token
    if (!token) return;

    // Check if token is expired and try to refresh
    if (isExpired()) {
      refreshAccessToken().catch((error) => {
        console.error('Token refresh failed:', error);
      });
    }

    // Set up token refresh interval
    // Backend access token TTL: 1 hour (3600000 ms)
    // Refresh after 54 minutes (leave 6 minute buffer before expiration)
    const refreshInterval = setInterval(() => {
      if (isExpired()) {
        refreshAccessToken().catch((error) => {
          console.error('Token refresh failed:', error);
        });
      }
    }, 54 * 60 * 1000); // 54 minutes

    return () => clearInterval(refreshInterval);
  }, [token, isExpired, refreshAccessToken]);

  return <>{children}</>;
};
