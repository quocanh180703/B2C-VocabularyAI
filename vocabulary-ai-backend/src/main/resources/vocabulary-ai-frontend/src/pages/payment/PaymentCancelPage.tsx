import { useNavigate } from "react-router-dom";
import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

/**
 * PaymentCancelPage - Displayed when user cancels PayOS payment
 * URL: /payment/cancel
 */
export function PaymentCancelPage() {
  const navigate = useNavigate();

  return (
    <div className="page">
      <div style={{ maxWidth: "500px", margin: "0 auto", paddingTop: "3rem" }}>
        {/* Cancel Icon */}
        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <div
            style={{
              display: "inline-flex",
              alignItems: "center",
              justifyContent: "center",
              width: "80px",
              height: "80px",
              backgroundColor: "#fee2e2",
              borderRadius: "50%",
              marginBottom: "1rem",
            }}
          >
            <Icon name="close" size="lg" style={{ color: "#dc2626" }} />
          </div>
        </div>

        {/* Main message */}
        <Card>
          <h1 style={{ textAlign: "center", margin: "0 0 0.5rem" }}>
            Payment Cancelled
          </h1>
          <p style={{ textAlign: "center", color: "#666", marginBottom: "2rem" }}>
            Your payment was not completed
          </p>

          {/* Info message */}
          <div style={{ backgroundColor: "#fef3c7", padding: "1rem", borderRadius: "0.375rem", marginBottom: "2rem" }}>
            <p style={{ margin: "0 0 0.5rem", fontWeight: 600, color: "#92400e" }}>
              What happened?
            </p>
            <ul
              style={{
                margin: "0.5rem 0 0",
                paddingLeft: "1.5rem",
                color: "#78350f",
                fontSize: "0.875rem",
              }}
            >
              <li>You cancelled the payment at the checkout page</li>
              <li>No charge was made to your payment method</li>
              <li>You can try again anytime</li>
            </ul>
          </div>

          {/* Tips */}
          <div style={{ backgroundColor: "#f0fdf4", padding: "1rem", borderRadius: "0.375rem", marginBottom: "2rem" }}>
            <p style={{ margin: "0 0 0.5rem", fontWeight: 600, color: "#15803d" }}>
              Tips
            </p>
            <ul
              style={{
                margin: "0.5rem 0 0",
                paddingLeft: "1.5rem",
                color: "#166534",
                fontSize: "0.875rem",
              }}
            >
              <li>Premium is optional - you can use the free tier forever</li>
              <li>Premium unlocks advanced features for serious learners</li>
              <li>You can upgrade anytime from your account</li>
            </ul>
          </div>

          {/* Action buttons */}
          <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
            <Button
              variant="primary"
              onClick={() => navigate("/pricing")}
              style={{ width: "100%" }}
            >
              Try Again
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate("/dashboard")}
              style={{ width: "100%" }}
            >
              Go to Dashboard
            </Button>
          </div>

          {/* Continue free info */}
          <div
            style={{
              textAlign: "center",
              marginTop: "1.5rem",
              padding: "1rem",
              backgroundColor: "#f9fafb",
              borderRadius: "0.375rem",
            }}
          >
            <p style={{ margin: 0, fontSize: "0.875rem", color: "#6b7280" }}>
              You can continue using all free features without upgrading.
            </p>
          </div>
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
          <p>Still interested in Premium? Check out the features that unlock.</p>
          <p>Having trouble? Contact our support team for assistance.</p>
        </div>
      </div>
    </div>
  );
}
