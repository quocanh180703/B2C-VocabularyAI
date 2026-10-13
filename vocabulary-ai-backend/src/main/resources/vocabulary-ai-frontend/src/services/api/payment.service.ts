/**
 * Payment Service
 * Handles all payment-related API calls
 * 
 * Communicates with backend PaymentController:
 * - GET /api/payments/packages
 * - POST /api/payments/initiate
 * - GET /api/payments/{orderId}
 * - GET /api/subscriptions/status
 * - POST /api/subscriptions/cancel
 * - GET /api/payments/history
 */

import { apiClient } from "./client";
import { PAYMENT_ENDPOINTS } from "./endpoints";
import {
  Package,
  InitiatePaymentResponse,
  PaymentOrder,
  SubscriptionStatus,
  PaymentHistoryPage,
  CancelSubscriptionRequest,
  CancelSubscriptionResponse,
  ApiResponse,
} from "./payment.types";

export class PaymentService {
  /**
   * Get all active packages available for purchase
   * @returns List of active packages
   */
  static async getPackages(): Promise<Package[]> {
    const response = await apiClient.get<ApiResponse<Package[]>>(
      PAYMENT_ENDPOINTS.GET_PACKAGES
    );
    return response.data.data || [];
  }

  /**
   * Initiate a payment for a specific package
   * User will be redirected to PayOS checkout URL
   * @param packageId Package ID to purchase
   * @returns Payment initiation response with checkout URL
   */
  static async initiatePayment(
    packageId: number
  ): Promise<InitiatePaymentResponse> {
    const response = await apiClient.post<ApiResponse<InitiatePaymentResponse>>(
      `${PAYMENT_ENDPOINTS.INITIATE_PAYMENT}?packageId=${packageId}`
    );
    return response.data.data!;
  }

  /**
   * Get payment order details
   * @param orderId Payment order ID
   * @returns Payment order details
   */
  static async getPaymentOrder(orderId: number): Promise<PaymentOrder> {
    const response = await apiClient.get<ApiResponse<PaymentOrder>>(
      PAYMENT_ENDPOINTS.GET_PAYMENT_ORDER(orderId.toString())
    );
    return response.data.data!;
  }

  /**
   * Get user's current subscription status
   * @returns Subscription status or null if no active subscription
   */
  static async getSubscriptionStatus(): Promise<SubscriptionStatus | null> {
    const response = await apiClient.get<ApiResponse<SubscriptionStatus>>(
      PAYMENT_ENDPOINTS.GET_SUBSCRIPTION_STATUS
    );
    return response.data.data || null;
  }

  /**
   * Cancel user's active subscription
   * @param subscriptionId Subscription ID to cancel
   * @returns Cancellation response
   */
  static async cancelSubscription(
    subscriptionId: number
  ): Promise<CancelSubscriptionResponse> {
    const request: CancelSubscriptionRequest = { subscriptionId };
    const response = await apiClient.post<
      ApiResponse<CancelSubscriptionResponse>
    >(PAYMENT_ENDPOINTS.CANCEL_SUBSCRIPTION, request);
    return response.data.data!;
  }

  /**
   * Get user's payment history with pagination
   * @param page Page number (0-indexed)
   * @param size Page size (default 10)
   * @returns Paginated payment history
   */
  static async getPaymentHistory(
    page: number = 0,
    size: number = 10
  ): Promise<PaymentHistoryPage> {
    const response = await apiClient.get<ApiResponse<PaymentHistoryPage>>(
      `${PAYMENT_ENDPOINTS.GET_PAYMENT_HISTORY}?page=${page}&size=${size}`
    );
    return response.data.data!;
  }
}
