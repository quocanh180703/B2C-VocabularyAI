import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import { authService } from '../services/api';
import { UserResponse } from '../services/api/types';

export interface AuthState {
  // State
  user: UserResponse | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  setUser: (user: AuthState['user']) => void;
  setToken: (token: string | null) => void;
  setRefreshToken: (refreshToken: string | null) => void;
  setIsLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;

  // Auth Actions
  login: (emailOrUsername: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;

  // Register Actions (2-step OTP)
  validateRegister: (email: string, username: string, password: string, fullName: string) => Promise<void>;
  verifyOtpRegister: (email: string, otp: string) => Promise<void>;

  // Forgot Password Actions (3-step OTP with auto-login)
  sendOtpForgotPassword: (email: string) => Promise<void>;
  verifyOtpForgotPassword: (email: string, otp: string) => Promise<string>; // Returns resetToken
  resetPassword: (email: string, newPassword: string, resetToken: string) => Promise<void>;

  // Computed
  isExpired: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  immer((set, get) => {
    // Load tokens from localStorage on initialization
    const storedToken = localStorage.getItem('vocabularyai_token');
    const storedRefreshToken = localStorage.getItem('vocabularyai_refresh_token');
    const storedUser = localStorage.getItem('vocabularyai_user');

    return {
      // Initial state
      user: storedUser ? JSON.parse(storedUser) : null,
      token: storedToken,
      refreshToken: storedRefreshToken,
      isAuthenticated: !!storedToken,
      isLoading: false,
      error: null,

      // Simple setters
      setUser: (user) => {
        set((state) => {
          state.user = user;
          state.isAuthenticated = !!user;
          if (user) {
            localStorage.setItem('vocabularyai_user', JSON.stringify(user));
          } else {
            localStorage.removeItem('vocabularyai_user');
          }
        });
      },

      setToken: (token) => {
        set((state) => {
          state.token = token;
          if (token) {
            localStorage.setItem('vocabularyai_token', token);
          } else {
            localStorage.removeItem('vocabularyai_token');
          }
        });
      },

      setRefreshToken: (refreshToken) => {
        set((state) => {
          state.refreshToken = refreshToken;
          if (refreshToken) {
            localStorage.setItem('vocabularyai_refresh_token', refreshToken);
          } else {
            localStorage.removeItem('vocabularyai_refresh_token');
          }
        });
      },

      setIsLoading: (loading) => {
        set((state) => {
          state.isLoading = loading;
        });
      },

      setError: (error) => {
        set((state) => {
          state.error = error;
        });
      },

      // Login
      login: async (emailOrUsername, password) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await authService.login({ emailOrUsername, password });
          
          set((state) => {
            state.user = response.user;
            state.token = response.access_token;
            state.refreshToken = response.refresh_token;
            state.isAuthenticated = true;
            state.isLoading = false;
          });

          // Persist to localStorage
          localStorage.setItem('vocabularyai_token', response.access_token);
          localStorage.setItem('vocabularyai_refresh_token', response.refresh_token);
          localStorage.setItem('vocabularyai_user', JSON.stringify(response.user));
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'Login failed';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Logout
      logout: async () => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const refreshToken = get().refreshToken;
          if (refreshToken) {
            await authService.logout(refreshToken);
          }
        } catch (error) {
          console.error('Logout error:', error);
        } finally {
          set((state) => {
            state.user = null;
            state.token = null;
            state.refreshToken = null;
            state.isAuthenticated = false;
            state.isLoading = false;
          });

          // Clear localStorage
          localStorage.removeItem('vocabularyai_token');
          localStorage.removeItem('vocabularyai_refresh_token');
          localStorage.removeItem('vocabularyai_user');
        }
      },

      // Refresh access token
      refreshAccessToken: async () => {
        const refreshToken = get().refreshToken;
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        try {
          const response = await authService.refreshToken({ refreshToken });
          set((state) => {
            state.token = response.access_token;
            state.refreshToken = response.refresh_token;
            state.user = response.user;
          });

          // Persist new tokens
          localStorage.setItem('vocabularyai_token', response.access_token);
          localStorage.setItem('vocabularyai_refresh_token', response.refresh_token);
          localStorage.setItem('vocabularyai_user', JSON.stringify(response.user));
        } catch (error) {
          // If refresh fails, logout the user
          get().logout();
          throw error;
        }
      },

      // Register - Step 1: Validate and send OTP
      validateRegister: async (email, username, password, fullName) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          await authService.validateRegister({ email, username, password, fullName });
          set((state) => {
            state.isLoading = false;
          });
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'Registration validation failed';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Register - Step 2: Verify OTP and create account
      verifyOtpRegister: async (email, otp) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          await authService.verifyOtpRegister({ email, otp });
          set((state) => {
            state.isLoading = false;
          });
          // NOTE: User must login manually after registration
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'OTP verification failed';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Forgot Password - Step 1: Send OTP
      sendOtpForgotPassword: async (email) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          await authService.sendOtpForgotPassword({ email });
          set((state) => {
            state.isLoading = false;
          });
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'Failed to send OTP';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Forgot Password - Step 2: Verify OTP
      verifyOtpForgotPassword: async (email, otp) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await authService.verifyOtpForgotPassword({ email, otp });
          set((state) => {
            state.isLoading = false;
          });
          return response.resetToken;
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'OTP verification failed';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Forgot Password - Step 3: Reset password (auto-login)
      resetPassword: async (email, newPassword, resetToken) => {
        set((state) => {
          state.isLoading = true;
          state.error = null;
        });

        try {
          const response = await authService.resetPassword({ email, newPassword, resetToken });
          set((state) => {
            state.user = response.user;
            state.token = response.access_token;
            state.refreshToken = response.refresh_token;
            state.isAuthenticated = true;
            state.isLoading = false;
          });

          // Persist to localStorage
          localStorage.setItem('vocabularyai_token', response.access_token);
          localStorage.setItem('vocabularyai_refresh_token', response.refresh_token);
          localStorage.setItem('vocabularyai_user', JSON.stringify(response.user));
        } catch (error) {
          set((state) => {
            state.error = error instanceof Error ? error.message : 'Password reset failed';
            state.isLoading = false;
          });
          throw error;
        }
      },

      // Computed property: Check if token is expired
      isExpired: () => {
        const token = get().token;
        if (!token) return true;

        try {
          // Decode JWT to check expiration
          const parts = token.split('.');
          if (parts.length !== 3) return true;

          const payload = JSON.parse(atob(parts[1]));
          const expirationTime = payload.exp * 1000; // Convert to ms
          return Date.now() >= expirationTime;
        } catch {
          return true;
        }
      },
    };
  })
);
