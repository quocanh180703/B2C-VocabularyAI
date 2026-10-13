import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";

interface SessionCompleteProps {
  wordsReviewed: number;
  wordsRemembered: number;
  accuracy: number;
  streakDays: number;
  onReviewDifficult: () => void;
  onViewProgress: () => void;
}

export function SessionComplete({
  wordsReviewed,
  wordsRemembered,
  accuracy,
  streakDays,
  onReviewDifficult,
  onViewProgress,
}: SessionCompleteProps) {
  return (
    <div className="session-complete">
      <div className="complete-mark">
        <Icon name="check" size="lg" />
      </div>
      <Badge tone="success">SESSION COMPLETE</Badge>
      <h1>Great work, Anh!</h1>
      <p>You reviewed {wordsReviewed} words and strengthened your memory today.</p>
      <Card className="results-card">
        <div>
          <strong>{wordsReviewed}</strong>
          <span>Words reviewed</span>
        </div>
        <div>
          <strong>{wordsRemembered}</strong>
          <span>Remembered</span>
        </div>
        <div>
          <strong>{wordsReviewed - wordsRemembered}</strong>
          <span>Need practice</span>
        </div>
      </Card>
      <div className="accuracy-block">
        <div>
          <span>Session accuracy</span>
          <strong>{accuracy}%</strong>
        </div>
        <ProgressBar value={accuracy} />
      </div>
      <div className="complete-streak">
        <Icon name="fire" />
        <div>
          <strong>{streakDays} day streak</strong>
          <p>Come back tomorrow to keep it going.</p>
        </div>
      </div>
      <div className="complete-actions">
        <Button variant="secondary" onClick={onReviewDifficult}>
          Review difficult words
        </Button>
        <Button onClick={onViewProgress}>
          View my progress <Icon name="arrow" />
        </Button>
      </div>
    </div>
  );
}
