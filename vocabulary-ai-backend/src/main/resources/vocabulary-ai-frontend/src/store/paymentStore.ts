/**
 * Payment Store - Zustand State Management
 * Manages payment and subscription state across the app
 */

import { create } from "zustand";
import { immer } from "zustand/middleware/immer";
import { paymentService } from "../services/api";
import {
  Package,
  SubscriptionStatus,
  PaymentHistoryPage,
  PaymentOrder,
} from "../services/api/payment.types";

export interface PaymentState {
  // State
  packages: Package[];
  currentSubscription: SubscriptionStatus | null;
  paymentHistory: PaymentHistoryPage | null;
  isLoading: boolean;
  error: string | null;

  // Computed
  hasActiveSubscription: () => boolean;
  isLoading: () => boolean;

  // Actions - Package Management
  fetchPackages: () => Promise<void>;

  // Actions - Payment
  initiatePayment: (packageId: number) => Promise<void>;
  getPaymentOrder: (orderId: number) => Promise<PaymentOrder | null>;

  // Actions - Subscription Management
  fetchSubscriptionStatus: () => Promise<void>;
  cancelSubscription: (subscriptionId: number) => Promise<void>;

  // Actions - Payment History
  fetchPaymentHistory: (page?: number, size?: number) => Promise<void>;

  // Utility
  clearError: () => void;
  reset: () => void;
}

export const usePaymentStore = create<PaymentState>()(
  immer((set, get) => ({
    // Initial state
    packages: [],
    currentSubscription: null,
    paymentHistory: null,
    isLoading: false,
    error: null,

    // Computed properties
    hasActiveSubscription: () => {
      const subscription = get().currentSubscription;
      return subscription !== null && subscription.status === "ACTIVE";
    },

    isLoading: () => get().isLoading,

    // Fetch packages
    fetchPackages: async () => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        const packages = await paymentService.getPackages();
        set((state) => {
          state.packages = packages;
          state.isLoading = false;
        });
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error ? error.message : "Failed to fetch packages";
          state.isLoading = false;
        });
        throw error;
      }
    },

    // Initiate payment
    initiatePayment: async (packageId: number) => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        const response = await paymentService.initiatePayment(packageId);
        set((state) => {
          state.isLoading = false;
        });

        // Redirect to PayOS checkout
        if (response.data.checkoutUrl) {
          window.location.href = response.data.checkoutUrl;
        }
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error
              ? error.message
              : "Failed to initiate payment";
          state.isLoading = false;
        });
        throw error;
      }
    },

    // Get payment order details
    getPaymentOrder: async (orderId: number) => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        const order = await paymentService.getPaymentOrder(orderId);
        set((state) => {
          state.isLoading = false;
        });
        return order;
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error
              ? error.message
              : "Failed to fetch payment order";
          state.isLoading = false;
        });
        return null;
      }
    },

    // Fetch subscription status
    fetchSubscriptionStatus: async () => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        const subscription =
          await paymentService.getSubscriptionStatus();
        set((state) => {
          state.currentSubscription = subscription;
          state.isLoading = false;
        });
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error
              ? error.message
              : "Failed to fetch subscription status";
          state.isLoading = false;
        });
        throw error;
      }
    },

    // Cancel subscription
    cancelSubscription: async (subscriptionId: number) => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        await paymentService.cancelSubscription(subscriptionId);
        set((state) => {
          state.currentSubscription = null;
          state.isLoading = false;
        });
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error
              ? error.message
              : "Failed to cancel subscription";
          state.isLoading = false;
        });
        throw error;
      }
    },

    // Fetch payment history
    fetchPaymentHistory: async (page = 0, size = 10) => {
      set((state) => {
        state.isLoading = true;
        state.error = null;
      });

      try {
        const history = await paymentService.getPaymentHistory(page, size);
        set((state) => {
          state.paymentHistory = history;
          state.isLoading = false;
        });
      } catch (error) {
        set((state) => {
          state.error =
            error instanceof Error
              ? error.message
              : "Failed to fetch payment history";
          state.isLoading = false;
        });
        throw error;
      }
    },

    // Clear error
    clearError: () => {
      set((state) => {
        state.error = null;
      });
    },

    // Reset store
    reset: () => {
      set(() => ({
        packages: [],
        currentSubscription: null,
        paymentHistory: null,
        isLoading: false,
        error: null,
      }));
    },
  }))
);
