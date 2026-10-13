import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface PlanStatusCardProps {
  onUpgrade: () => void;
}

export function PlanStatusCard({ onUpgrade }: PlanStatusCardProps) {
  return (
    <Card className="plan-status-card">
      <span className="plan-status-icon">
        <Icon name="star" />
      </span>
      <div>
        <strong>Free Plan</strong>
        <p>Unlock more AI-powered learning features.</p>
      </div>
      <Button variant="secondary" onClick={onUpgrade}>
        Upgrade to Premium
      </Button>
    </Card>
  );
}
