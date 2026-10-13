import { useEffect, useState } from "react";
import { usePayment } from "@/hooks/usePayment";
import { useAuth } from "@/hooks/useAuth";
import { PackageCard, ComparisonTable } from "@/components/payment";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import type { Package } from "@/services/api";

interface PricingPackageCardProps {
  name: string;
  price: string | number;
  duration: string;
  description: string;
  features: Array<{ icon: string; label: string }>;
  isPopular?: boolean;
  onSelect: () => void;
  isLoading?: boolean;
}

export function PricingPage() {
  const { packages, isLoading, error, fetchPackages, initiatePayment, hasActiveSubscription } =
    usePayment();
  const { isAuthenticated } = useAuth();
  const [selectedPackageId, setSelectedPackageId] = useState<number | null>(null);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  // Fetch packages on mount
  useEffect(() => {
    fetchPackages().catch((err) => {
      console.error("Failed to fetch packages:", err);
    });
  }, [fetchPackages]);

  // Handle package selection
  const handleSelectPackage = async (packageId: number) => {
    setPaymentError(null);

    if (!isAuthenticated) {
      setPaymentError("Please log in to purchase a subscription");
      return;
    }

    if (hasActiveSubscription()) {
      setPaymentError(
        "You already have an active subscription. Please cancel it first."
      );
      return;
    }

    try {
      setSelectedPackageId(packageId);
      await initiatePayment(packageId);
      // User will be redirected to PayOS checkout
    } catch (err) {
      setPaymentError(
        err instanceof Error ? err.message : "Failed to initiate payment"
      );
      setSelectedPackageId(null);
    }
  };

  // Format price in VND
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(price);
  };

  // Map backend packages to component props
  const displayPackages: PricingPackageCardProps[] = packages.map((pkg, index) => ({
    name: pkg.name,
    price: formatPrice(pkg.price),
    duration: pkg.durationDays === 30 ? "month" : pkg.durationDays === 365 ? "year" : "days",
    description: pkg.description || "Premium features",
    features: [
      { icon: "check", label: "Unlimited words" },
      { icon: "check", label: "Advanced spaced repetition" },
      { icon: "check", label: "Priority AI support" },
      { icon: "check", label: "Unlimited AI explanations" },
      { icon: "check", label: "Grammar checking" },
      { icon: "check", label: "Speaking practice" },
      { icon: "check", label: "Progress analytics" },
    ],
    isPopular: index === 0, // Mark first premium package as popular
    onSelect: () => handleSelectPackage(pkg.id),
    isLoading: selectedPackageId === pkg.id && isLoading,
  }));

  const comparisonFeatures = [
    { name: "Words per month", free: "50", premium: "Unlimited" },
    { name: "Spaced repetition", free: "Basic", premium: "Advanced" },
    { name: "AI explanations", free: "Limited", premium: "Unlimited" },
    { name: "Grammar check", free: false, premium: true },
    { name: "Speaking practice", free: false, premium: true },
    { name: "Progress analytics", free: false, premium: true },
    { name: "Support", free: "Community", premium: "Priority" },
  ];

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">UPGRADE YOUR LEARNING</p>
          <h1>Premium membership</h1>
          <p>Unlock all features and accelerate your English learning.</p>
        </div>
      </div>

      {/* Error message display */}
      {(error || paymentError) && (
        <Card className="error-card" style={{ borderColor: "#dc2626", backgroundColor: "#fee2e2" }}>
          <p style={{ color: "#dc2626" }}>
            {error || paymentError}
          </p>
        </Card>
      )}

      {/* Loading state */}
      {isLoading && packages.length === 0 && (
        <div style={{ textAlign: "center", padding: "2rem" }}>
          <p>Loading packages...</p>
        </div>
      )}

      {/* Packages grid */}
      {packages.length > 0 && (
        <div className="pricing-grid">
          {displayPackages.map((pkg) => (
            <PackageCard
              key={pkg.name}
              name={pkg.name}
              price={pkg.price}
              duration={pkg.duration}
              description={pkg.description}
              features={pkg.features}
              isPopular={pkg.isPopular}
              onSelect={pkg.onSelect}
            />
          ))}
        </div>
      )}

      <Card className="billing-info">
        <div className="billing-header">
          <h2>How it works</h2>
          <p>Simple and transparent pricing</p>
        </div>
        <div className="billing-options">
          <div className="option">
            <strong>Choose a plan</strong>
            <p>Select the subscription that fits your learning goals</p>
          </div>
          <div className="option">
            <strong>Secure payment</strong>
            <p>Pay securely through PayOS (accepts credit cards, bank transfers, etc.)</p>
          </div>
          <div className="option">
            <strong>Instant access</strong>
            <p>Start using premium features immediately after payment</p>
          </div>
        </div>
        <p className="billing-note">
          {isAuthenticated
            ? hasActiveSubscription()
              ? "You have an active subscription. You can manage it from your account settings."
              : "Ready to upgrade? Choose a plan above to get started."
            : "Please log in to purchase a subscription."}
        </p>
      </Card>

      <div className="comparison-section">
        <h2>Feature comparison</h2>
        <p>See what's included in each plan</p>
        <ComparisonTable features={comparisonFeatures} />
      </div>

      <Card className="faq-card">
        <h2>Frequently asked questions</h2>
        <div className="faq-items">
          <div className="faq-item">
            <strong>Can I cancel anytime?</strong>
            <p>
              Yes, you can cancel your subscription at any time from your account settings. Your
              access will be available until the end of your billing period.
            </p>
          </div>
          <div className="faq-item">
            <strong>What payment methods do you accept?</strong>
            <p>
              We accept all major credit cards, bank transfers, e-wallets, and other payment methods
              through PayOS.
            </p>
          </div>
          <div className="faq-item">
            <strong>How do refunds work?</strong>
            <p>
              Refund requests are handled on a case-by-case basis. Please contact our support team
              if you have questions about your payment.
            </p>
          </div>
          <div className="faq-item">
            <strong>Do I need a subscription?</strong>
            <p>
              No! You can use the free tier with basic features. A subscription unlocks advanced
              features for serious learners.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
