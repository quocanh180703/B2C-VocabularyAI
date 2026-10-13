/**
 * usePayment Hook
 * Simplified access to payment store
 * 
 * Usage:
 * const { packages, fetchPackages, initiatePayment, hasActiveSubscription } = usePayment();
 */

import { usePaymentStore } from "../store/paymentStore";

export function usePayment() {
  // State
  const packages = usePaymentStore((state) => state.packages);
  const currentSubscription = usePaymentStore(
    (state) => state.currentSubscription
  );
  const paymentHistory = usePaymentStore((state) => state.paymentHistory);
  const isLoading = usePaymentStore((state) => state.isLoading);
  const error = usePaymentStore((state) => state.error);

  // Computed
  const hasActiveSubscription = usePaymentStore(
    (state) => state.hasActiveSubscription
  );

  // Actions
  const fetchPackages = usePaymentStore((state) => state.fetchPackages);
  const initiatePayment = usePaymentStore((state) => state.initiatePayment);
  const getPaymentOrder = usePaymentStore((state) => state.getPaymentOrder);
  const fetchSubscriptionStatus = usePaymentStore(
    (state) => state.fetchSubscriptionStatus
  );
  const cancelSubscription = usePaymentStore(
    (state) => state.cancelSubscription
  );
  const fetchPaymentHistory = usePaymentStore(
    (state) => state.fetchPaymentHistory
  );
  const clearError = usePaymentStore((state) => state.clearError);
  const reset = usePaymentStore((state) => state.reset);

  return {
    // State
    packages,
    currentSubscription,
    paymentHistory,
    isLoading,
    error,

    // Computed
    hasActiveSubscription,

    // Actions
    fetchPackages,
    initiatePayment,
    getPaymentOrder,
    fetchSubscriptionStatus,
    cancelSubscription,
    fetchPaymentHistory,
    clearError,
    reset,
  };
}
