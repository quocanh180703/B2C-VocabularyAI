import { Card } from "@/components/common/Card";
import { Icon } from "@/components/common/Icon";
import { IconName } from "@/types";

interface StatsCardProps {
  icon: IconName;
  value: string;
  label: string;
  tone: "amber" | "indigo" | "green" | "violet";
}

export function StatsCard({ icon, value, label, tone }: StatsCardProps) {
  return (
    <Card className="stat-card">
      <span className={`stat-icon stat-${tone}`}>
        <Icon name={icon} />
      </span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </Card>
  );
}
