import { useEffect, useState } from "react";
import { usePayment } from "@/hooks/usePayment";
import { PaymentHistoryTable } from "@/components/payment";
import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";

/**
 * PaymentHistoryPage - Display user's payment order history
 */
export function PaymentHistoryPage() {
  const { paymentHistory, isLoading, error, fetchPaymentHistory } = usePayment();
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Fetch payment history on mount and page change
  useEffect(() => {
    fetchPaymentHistory(currentPage, pageSize).catch((err) => {
      console.error("Failed to fetch payment history:", err);
    });
  }, [currentPage, fetchPaymentHistory]);

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">ACCOUNT</p>
          <h1>Payment History</h1>
          <p>View all your subscription purchases and transactions</p>
        </div>
      </div>

      {/* Error message */}
      {error && (
        <Card style={{ borderColor: "#dc2626", backgroundColor: "#fee2e2", marginBottom: "1rem" }}>
          <p style={{ color: "#dc2626", margin: 0 }}>
            Error: {error}
          </p>
        </Card>
      )}

      {/* Payment history table */}
      <Card>
        <PaymentHistoryTable
          items={paymentHistory?.content || []}
          isLoading={isLoading}
        />
      </Card>

      {/* Pagination controls */}
      {paymentHistory && paymentHistory.totalPages > 1 && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "1rem", marginTop: "2rem" }}>
          <Button
            variant="secondary"
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0 || isLoading}
          >
            ← Previous
          </Button>

          <div style={{ color: "#6b7280", fontSize: "0.875rem" }}>
            Page {paymentHistory.currentPage + 1} of {paymentHistory.totalPages}
            {paymentHistory.totalElements > 0 && (
              <>
                {" "}
                ({paymentHistory.totalElements} total)
              </>
            )}
          </div>

          <Button
            variant="secondary"
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={currentPage >= paymentHistory.totalPages - 1 || isLoading}
          >
            Next →
          </Button>
        </div>
      )}

      {/* Empty state */}
      {!isLoading && paymentHistory && paymentHistory.content.length === 0 && (
        <Card style={{ textAlign: "center", padding: "3rem" }}>
          <p style={{ color: "#6b7280" }}>
            You don't have any payment history yet.
          </p>
          <p style={{ fontSize: "0.875rem", color: "#9ca3af" }}>
            When you purchase a subscription, it will appear here.
          </p>
        </Card>
      )}
    </div>
  );
}
