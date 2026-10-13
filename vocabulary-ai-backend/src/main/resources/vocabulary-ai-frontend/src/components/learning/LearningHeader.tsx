import { Button } from "@/components/common/Button";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";

interface LearningHeaderProps {
  current: number;
  total: number;
  timeElapsed: string;
  onExit: () => void;
}

export function LearningHeader({
  current,
  total,
  timeElapsed,
  onExit,
}: LearningHeaderProps) {
  const progress = ((current + 1) / total) * 100;

  return (
    <div className="learn-header">
      <Button variant="ghost" onClick={onExit}>
        ← Exit review
      </Button>
      <div className="learn-progress">
        <div>
          <span>Today's review</span>
          <strong>
            {current + 1} of {total}
          </strong>
        </div>
        <ProgressBar value={progress} />
      </div>
      <span className="session-time">
        <Icon name="clock" /> {timeElapsed}
      </span>
    </div>
  );
}
