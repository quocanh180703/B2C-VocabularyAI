import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { usePayment } from "@/hooks/usePayment";
import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

/**
 * PaymentSuccessPage - Displayed after successful PayOS payment
 * URL: /payment/success?orderId=1
 */
export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderId = searchParams.get("orderId");
  
  const { getPaymentOrder, currentSubscription, fetchSubscriptionStatus, isLoading } =
    usePayment();
  const [paymentOrder, setPaymentOrder] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [countdown, setCountdown] = useState(5);

  // Fetch payment order and subscription on mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        if (orderId) {
          const order = await getPaymentOrder(parseInt(orderId));
          setPaymentOrder(order);
        }
        await fetchSubscriptionStatus();
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load payment details"
        );
      }
    };

    fetchData();
  }, [orderId, getPaymentOrder, fetchSubscriptionStatus]);

  // Countdown timer for auto-redirect
  useEffect(() => {
    if (countdown > 0 && !error) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else if (countdown === 0 && !error) {
      navigate("/dashboard");
    }
  }, [countdown, navigate, error]);

  return (
    <div className="page">
      <div style={{ maxWidth: "500px", margin: "0 auto", paddingTop: "3rem" }}>
        {/* Success Icon */}
        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <div
            style={{
              display: "inline-flex",
              alignItems: "center",
              justifyContent: "center",
              width: "80px",
              height: "80px",
              backgroundColor: "#d1fae5",
              borderRadius: "50%",
              marginBottom: "1rem",
            }}
          >
            <Icon name="checkmark" size="lg" style={{ color: "#059669" }} />
          </div>
        </div>

        {/* Main message */}
        <Card>
          <h1 style={{ textAlign: "center", margin: "0 0 0.5rem" }}>
            Payment Successful! 🎉
          </h1>
          <p style={{ textAlign: "center", color: "#666", marginBottom: "2rem" }}>
            Your subscription has been activated
          </p>

          {/* Payment details */}
          {paymentOrder && (
            <div style={{ backgroundColor: "#f9fafb", padding: "1rem", borderRadius: "0.375rem", marginBottom: "1.5rem" }}>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  marginBottom: "0.5rem",
                }}
              >
                <span style={{ color: "#6b7280" }}>Order Code:</span>
                <span style={{ fontWeight: 600 }}>{paymentOrder.orderCode}</span>
              </div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  marginBottom: "0.5rem",
                }}
              >
                <span style={{ color: "#6b7280" }}>Amount:</span>
                <span style={{ fontWeight: 600 }}>
                  {new Intl.NumberFormat("vi-VN", {
                    style: "currency",
                    currency: "VND",
                    minimumFractionDigits: 0,
                  }).format(paymentOrder.amount)}
                </span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "#6b7280" }}>Status:</span>
                <span
                  style={{
                    backgroundColor: "#ecfdf5",
                    color: "#065f46",
                    padding: "0.25rem 0.75rem",
                    borderRadius: "0.25rem",
                    fontWeight: 600,
                  }}
                >
                  {paymentOrder.status}
                </span>
              </div>
            </div>
          )}

          {/* Active subscription message */}
          {currentSubscription && (
            <div style={{ backgroundColor: "#f0fdf4", padding: "1rem", borderRadius: "0.375rem", marginBottom: "1.5rem" }}>
              <p style={{ margin: 0, color: "#15803d", fontWeight: 600 }}>
                ✓ Your {currentSubscription.packageName} subscription is now active
              </p>
              <p style={{ margin: "0.5rem 0 0", color: "#166534", fontSize: "0.875rem" }}>
                Valid until {new Date(currentSubscription.endDate).toLocaleDateString("en-US")}
              </p>
            </div>
          )}

          {/* What's next */}
          <div style={{ backgroundColor: "#eff6ff", padding: "1rem", borderRadius: "0.375rem", marginBottom: "1.5rem" }}>
            <p style={{ margin: "0 0 0.5rem", fontWeight: 600, color: "#0c4a6e" }}>
              What's Next?
            </p>
            <ul
              style={{
                margin: "0.5rem 0 0",
                paddingLeft: "1.5rem",
                color: "#075985",
                fontSize: "0.875rem",
              }}
            >
              <li>Explore all premium features on your dashboard</li>
              <li>Start your first premium learning session</li>
              <li>Access advanced AI tools for better learning</li>
              <li>Track your progress with detailed analytics</li>
            </ul>
          </div>

          {/* Error message */}
          {error && (
            <div style={{ backgroundColor: "#fee2e2", padding: "1rem", borderRadius: "0.375rem", marginBottom: "1.5rem" }}>
              <p style={{ margin: 0, color: "#dc2626" }}>
                ⚠️ {error}
              </p>
            </div>
          )}

          {/* Action buttons */}
          <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
            <Button
              variant="primary"
              onClick={() => navigate("/dashboard")}
              style={{ width: "100%" }}
            >
              Go to Dashboard
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate("/pricing")}
              style={{ width: "100%" }}
            >
              Back to Plans
            </Button>
          </div>

          {/* Auto-redirect timer */}
          {!error && (
            <p
              style={{
                textAlign: "center",
                marginTop: "1rem",
                color: "#9ca3af",
                fontSize: "0.875rem",
              }}
            >
              Auto-redirecting in {countdown} seconds...
            </p>
          )}
        </Card>

        {/* Support info */}
        <div
          style={{
            textAlign: "center",
            marginTop: "2rem",
            padding: "1rem",
            color: "#6b7280",
            fontSize: "0.875rem",
          }}
        >
          <p>Questions? Check your email for the receipt and subscription details.</p>
          <p>Need help? Contact our support team.</p>
        </div>
      </div>
    </div>
  );
}
