/**
 * Payment & Subscription Type Definitions
 * DTOs matching backend PaymentService responses
 */

/**
 * Package - Subscription package available for purchase
 */
export interface Package {
  id: number;
  code: string; // PREMIUM_MONTHLY, PREMIUM_YEARLY
  name: string;
  price: number; // VND
  durationDays: number;
  description: string;
}

/**
 * InitiatePaymentResponse - Response from POST /api/payments/initiate
 */
export interface InitiatePaymentResponse {
  code: string; // "00" = success
  desc: string;
  data: {
    checkoutUrl: string; // PayOS checkout URL
    transactionCode: string;
    qrCode: string;
    amount: number;
    orderId: string;
    status: string;
  };
}

/**
 * PaymentOrder - Payment order entity
 */
export interface PaymentOrder {
  id: number;
  userId: number;
  packageId: number;
  orderCode: string; // ORD_timestamp_randomId
  amount: number; // VND
  status: "PENDING" | "PAID" | "FAILED" | "CANCELLED";
  paymentUrl: string; // PayOS checkout URL
  paidAt?: string; // ISO 8601 datetime
  createdAt: string;
  updatedAt: string;
}

/**
 * SubscriptionStatus - User's current subscription status
 */
export interface SubscriptionStatus {
  subscriptionId: number;
  userId: number;
  packageName: string;
  status: "ACTIVE" | "EXPIRED" | "CANCELLED" | "SUSPENDED";
  remainingDays: number;
  endDate: string; // ISO 8601 datetime
}

/**
 * PaymentHistoryItem - Single payment order in history
 */
export interface PaymentHistoryItem {
  id: number;
  orderCode: string;
  amount: number; // VND
  status: "PENDING" | "PAID" | "FAILED" | "CANCELLED";
  paidAt?: string;
  packageName: string;
  transactionCode?: string;
  createdAt: string;
}

/**
 * PaymentHistoryPage - Paginated payment history response
 */
export interface PaymentHistoryPage {
  content: PaymentHistoryItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

/**
 * CancelSubscriptionRequest - Request to cancel subscription
 */
export interface CancelSubscriptionRequest {
  subscriptionId: number;
}

/**
 * CancelSubscriptionResponse - Response after cancelling subscription
 */
export interface CancelSubscriptionResponse {
  subscriptionId: number;
  status: "CANCELLED";
  cancelledAt: string;
}

/**
 * ApiResponse wrapper for all API responses
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}
