import { Icon } from "@/components/common/Icon";
import { Button } from "@/components/common/Button";

interface ErrorStateProps {
  title?: string;
  description?: string;
  error?: Error;
  onRetry: () => void;
  onContact?: () => void;
}

/**
 * Error State Component
 * Displays when data fetching fails with retry options
 */
export function ErrorState({
  title = "Unable to load dashboard",
  description,
  error,
  onRetry,
  onContact
}: ErrorStateProps) {
  const errorMessage = description || error?.message || "Something went wrong. Please try again.";

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Error</p>
          <h1>{title}</h1>
          <p>{errorMessage}</p>
        </div>
      </div>

      <div style={{
        padding: "40px 20px",
        textAlign: "center",
        marginTop: "2rem"
      }}>
        <Icon name="alert" size="lg" style={{ opacity: 0.5, marginBottom: "16px" }} />
        
        <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
          <Button onClick={onRetry}>
            Try Again
          </Button>
          {onContact && (
            <Button variant="secondary" onClick={onContact}>
              Contact Support
            </Button>
          )}
        </div>

        <p style={{
          marginTop: "20px",
          fontSize: "12px",
          color: "#999"
        }}>
          If the problem persists, check your internet connection or contact our support team.
        </p>
      </div>
    </div>
  );
}
