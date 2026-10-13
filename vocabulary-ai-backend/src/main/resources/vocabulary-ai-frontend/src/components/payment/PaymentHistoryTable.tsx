import { Icon } from "@/components/common/Icon";
import type { PaymentHistoryItem } from "@/services/api";

interface PaymentHistoryTableProps {
  items: PaymentHistoryItem[];
  isLoading?: boolean;
}

/**
 * PaymentHistoryTable - Displays paginated list of payment orders
 */
export function PaymentHistoryTable({ items, isLoading }: PaymentHistoryTableProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(price);
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case "PAID":
        return { bg: "#ecfdf5", text: "#065f46", border: "#86efac" };
      case "PENDING":
        return { bg: "#fffbeb", text: "#92400e", border: "#fcd34d" };
      case "FAILED":
      case "CANCELLED":
        return { bg: "#fee2e2", text: "#991b1b", border: "#fca5a5" };
      default:
        return { bg: "#f3f4f6", text: "#374151", border: "#d1d5db" };
    }
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: "center", padding: "2rem" }}>
        <p>Loading payment history...</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div style={{ textAlign: "center", padding: "2rem" }}>
        <Icon name="inbox" size="lg" style={{ marginBottom: "1rem", opacity: 0.5 }} />
        <p style={{ color: "#6b7280" }}>No payment history yet</p>
      </div>
    );
  }

  return (
    <div style={{ overflowX: "auto" }}>
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ borderBottom: "2px solid #e5e7eb" }}>
            <th
              style={{
                textAlign: "left",
                padding: "0.75rem",
                fontWeight: 600,
                color: "#374151",
                fontSize: "0.875rem",
              }}
            >
              Order Code
            </th>
            <th
              style={{
                textAlign: "left",
                padding: "0.75rem",
                fontWeight: 600,
                color: "#374151",
                fontSize: "0.875rem",
              }}
            >
              Package
            </th>
            <th
              style={{
                textAlign: "right",
                padding: "0.75rem",
                fontWeight: 600,
                color: "#374151",
                fontSize: "0.875rem",
              }}
            >
              Amount
            </th>
            <th
              style={{
                textAlign: "center",
                padding: "0.75rem",
                fontWeight: 600,
                color: "#374151",
                fontSize: "0.875rem",
              }}
            >
              Status
            </th>
            <th
              style={{
                textAlign: "left",
                padding: "0.75rem",
                fontWeight: 600,
                color: "#374151",
                fontSize: "0.875rem",
              }}
            >
              Date
            </th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const statusColor = getStatusBadgeColor(item.status);
            return (
              <tr
                key={item.id}
                style={{
                  borderBottom: "1px solid #e5e7eb",
                  "&:hover": { backgroundColor: "#f9fafb" },
                }}
              >
                <td style={{ padding: "1rem 0.75rem", fontSize: "0.875rem" }}>
                  <code
                    style={{
                      backgroundColor: "#f3f4f6",
                      padding: "0.25rem 0.5rem",
                      borderRadius: "0.25rem",
                      fontFamily: "monospace",
                    }}
                  >
                    {item.orderCode}
                  </code>
                </td>
                <td style={{ padding: "1rem 0.75rem", fontSize: "0.875rem" }}>
                  {item.packageName}
                </td>
                <td
                  style={{
                    padding: "1rem 0.75rem",
                    textAlign: "right",
                    fontSize: "0.875rem",
                    fontWeight: 500,
                  }}
                >
                  {formatPrice(item.amount)}
                </td>
                <td style={{ padding: "1rem 0.75rem", textAlign: "center" }}>
                  <span
                    style={{
                      display: "inline-block",
                      backgroundColor: statusColor.bg,
                      color: statusColor.text,
                      padding: "0.375rem 0.75rem",
                      borderRadius: "0.25rem",
                      fontSize: "0.75rem",
                      fontWeight: 600,
                      border: `1px solid ${statusColor.border}`,
                    }}
                  >
                    {item.status}
                  </span>
                </td>
                <td style={{ padding: "1rem 0.75rem", fontSize: "0.875rem", color: "#6b7280" }}>
                  {item.paidAt ? formatDate(item.paidAt) : formatDate(item.createdAt)}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
