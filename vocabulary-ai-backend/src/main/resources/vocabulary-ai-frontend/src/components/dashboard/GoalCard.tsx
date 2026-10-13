import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";
import { IconName } from "@/types";

interface GoalItem {
  icon: IconName;
  title: string;
  description: string;
  badge?: {
    tone: "success" | "warning" | "neutral";
    label: string;
  };
  isActive?: boolean;
}

interface GoalCardProps {
  progress: number;
  completed: number;
  total: number;
  goals: GoalItem[];
}

export function GoalCard({ progress, completed, total, goals }: GoalCardProps) {
  return (
    <Card className="today-card">
      <div className="section-title">
        <div>
          <h3>Today's goal</h3>
          <p>
            {completed} of {total} words completed
          </p>
        </div>
        <strong>{progress}%</strong>
      </div>
      <ProgressBar value={progress} />
      <div className="goal-list">
        {goals.map((goal) => (
          <div key={goal.title}>
            {goal.isActive ? (
              <span className="goal-active">
                <Icon name={goal.icon} size="sm" />
              </span>
            ) : (
              <span className="goal-check">
                <Icon name="check" size="sm" />
              </span>
            )}
            <div>
              <strong>{goal.title}</strong>
              <p>{goal.description}</p>
            </div>
            {goal.badge && <Badge tone={goal.badge.tone}>{goal.badge.label}</Badge>}
            {!goal.badge && !goal.isActive && <Icon name="chevron" size="sm" />}
          </div>
        ))}
      </div>
    </Card>
  );
}
