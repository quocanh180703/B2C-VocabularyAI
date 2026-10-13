import { useEffect, useState } from "react";
import { usePayment } from "@/hooks/usePayment";
import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import type { SubscriptionStatus } from "@/services/api";

interface SubscriptionCardProps {
  onSubscriptionCancel?: () => void;
}

/**
 * SubscriptionCard - Displays user's active subscription status
 * Shows package name, status, remaining days, and expiration date
 * Provides button to cancel subscription
 */
export function SubscriptionCard({ onSubscriptionCancel }: SubscriptionCardProps) {
  const { currentSubscription, isLoading, error, fetchSubscriptionStatus, cancelSubscription } =
    usePayment();
  const [isCancelling, setIsCancelling] = useState(false);
  const [cancelError, setCancelError] = useState<string | null>(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  // Fetch subscription status on mount
  useEffect(() => {
    fetchSubscriptionStatus().catch((err) => {
      console.error("Failed to fetch subscription status:", err);
    });
  }, [fetchSubscriptionStatus]);

  // Handle subscription cancellation
  const handleCancelSubscription = async () => {
    if (!currentSubscription) return;

    setCancelError(null);
    setIsCancelling(true);

    try {
      await cancelSubscription(currentSubscription.subscriptionId);
      setShowCancelConfirm(false);
      onSubscriptionCancel?.();
    } catch (err) {
      setCancelError(
        err instanceof Error ? err.message : "Failed to cancel subscription"
      );
    } finally {
      setIsCancelling(false);
    }
  };

  // Format date display
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  // Loading state
  if (isLoading && !currentSubscription) {
    return (
      <Card className="subscription-card">
        <div style={{ textAlign: "center", padding: "2rem" }}>
          <p>Loading subscription info...</p>
        </div>
      </Card>
    );
  }

  // No subscription state
  if (!currentSubscription) {
    return (
      <Card className="subscription-card no-subscription">
        <div style={{ textAlign: "center", padding: "2rem" }}>
          <Icon name="star" size="lg" style={{ marginBottom: "1rem" }} />
          <h3>No Active Subscription</h3>
          <p>You don't have an active subscription yet.</p>
          <p style={{ fontSize: "0.875rem", color: "#666" }}>
            Upgrade to Premium to unlock advanced features
          </p>
          <Button
            variant="primary"
            onClick={() => {
              window.location.href = "/pricing";
            }}
            style={{ marginTop: "1rem" }}
          >
            View Plans
          </Button>
        </div>
      </Card>
    );
  }

  // Active subscription state
  const daysRemaining = currentSubscription.remainingDays;
  const statusColor =
    daysRemaining > 30
      ? "#10b981" // green
      : daysRemaining > 7
        ? "#f59e0b" // amber
        : "#ef4444"; // red

  return (
    <Card className="subscription-card active-subscription">
      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", marginBottom: "1.5rem" }}>
        <Icon name="checkmark-circle" size="lg" style={{ color: "#10b981", marginRight: "0.5rem" }} />
        <div>
          <h3 style={{ margin: 0 }}>{currentSubscription.packageName}</h3>
          <p style={{ margin: "0.25rem 0 0", color: "#666", fontSize: "0.875rem" }}>
            Status: <span style={{ color: statusColor, fontWeight: 600 }}>
              {currentSubscription.status}
            </span>
          </p>
        </div>
      </div>

      {/* Subscription details */}
      <div style={{ marginBottom: "1.5rem" }}>
        <div style={{ marginBottom: "1rem" }}>
          <p style={{ margin: "0 0 0.5rem", fontSize: "0.875rem", color: "#666" }}>
            Days Remaining
          </p>
          <div
            style={{
              fontSize: "1.875rem",
              fontWeight: 700,
              color: statusColor,
            }}
          >
            {daysRemaining} days
          </div>
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <p style={{ margin: "0 0 0.5rem", fontSize: "0.875rem", color: "#666" }}>
            Expires On
          </p>
          <p style={{ margin: 0, fontSize: "1rem" }}>
            {formatDate(currentSubscription.endDate)}
          </p>
        </div>

        {/* Warning for expiring soon */}
        {daysRemaining <= 30 && daysRemaining > 0 && (
          <div
            style={{
              padding: "0.75rem",
              backgroundColor: "#fef3c7",
              border: "1px solid #fcd34d",
              borderRadius: "0.375rem",
              marginTop: "1rem",
            }}
          >
            <p style={{ margin: 0, fontSize: "0.875rem", color: "#92400e" }}>
              ⚠️ Your subscription expires soon. Consider renewing to maintain access.
            </p>
          </div>
        )}
      </div>

      {/* Cancel button */}
      {!showCancelConfirm ? (
        <Button
          variant="secondary"
          onClick={() => setShowCancelConfirm(true)}
          disabled={isCancelling}
          style={{ width: "100%", marginTop: "1rem" }}
        >
          Cancel Subscription
        </Button>
      ) : (
        <div style={{ marginTop: "1rem" }}>
          <div
            style={{
              padding: "1rem",
              backgroundColor: "#fee2e2",
              border: "1px solid #fecaca",
              borderRadius: "0.375rem",
              marginBottom: "1rem",
            }}
          >
            <p style={{ margin: "0 0 0.5rem", fontWeight: 600, color: "#991b1b" }}>
              Cancel Subscription?
            </p>
            <p style={{ margin: 0, color: "#7f1d1d", fontSize: "0.875rem" }}>
              You will lose access to premium features. This action cannot be undone.
            </p>
          </div>
          <div style={{ display: "flex", gap: "0.75rem" }}>
            <Button
              variant="primary"
              onClick={() => setShowCancelConfirm(false)}
              disabled={isCancelling}
              style={{ flex: 1 }}
            >
              Keep Subscription
            </Button>
            <Button
              variant="secondary"
              onClick={handleCancelSubscription}
              disabled={isCancelling}
              style={{
                flex: 1,
                backgroundColor: "#ef4444",
                color: "white",
                border: "none",
              }}
            >
              {isCancelling ? "Cancelling..." : "Yes, Cancel"}
            </Button>
          </div>
        </div>
      )}

      {/* Error message */}
      {(error || cancelError) && (
        <div
          style={{
            padding: "0.75rem",
            backgroundColor: "#fee2e2",
            border: "1px solid #fecaca",
            borderRadius: "0.375rem",
            marginTop: "1rem",
          }}
        >
          <p style={{ margin: 0, fontSize: "0.875rem", color: "#dc2626" }}>
            {error || cancelError}
          </p>
        </div>
      )}
    </Card>
  );
}
