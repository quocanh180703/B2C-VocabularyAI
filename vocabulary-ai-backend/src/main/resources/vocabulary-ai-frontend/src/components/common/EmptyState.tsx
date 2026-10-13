import { Icon } from "@/components/common/Icon";
import { IconName } from "@/types";

interface EmptyStateProps {
  icon?: IconName;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

/**
 * Generic EmptyState Component
 * Used when a section has no data to display
 */
export function EmptyState({ 
  icon = "inbox", 
  title, 
  description, 
  action 
}: EmptyStateProps) {
  return (
    <div style={{
      textAlign: "center",
      padding: "40px 20px",
      color: "#666"
    }}>
      <Icon name={icon} size="lg" style={{ opacity: 0.5, marginBottom: "16px" }} />
      <h3 style={{ margin: "12px 0", color: "#333" }}>{title}</h3>
      <p style={{ margin: "8px 0 20px", fontSize: "14px" }}>{description}</p>
      {action && (
        <button
          onClick={action.onClick}
          style={{
            padding: "8px 16px",
            backgroundColor: "#6366f1",
            color: "white",
            border: "none",
            borderRadius: "6px",
            cursor: "pointer",
            fontSize: "14px",
            fontWeight: "500"
          }}
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
