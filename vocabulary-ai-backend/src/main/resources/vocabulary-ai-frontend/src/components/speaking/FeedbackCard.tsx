import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";

interface FeedbackMetric {
  label: string;
  score: string;
}

interface FeedbackCardProps {
  title: string;
  subtitle: string;
  score: number;
  metrics: FeedbackMetric[];
  transcript: string;
  suggestion: string;
  feedback: string;
  onRetry: () => void;
  onNext: () => void;
}

export function FeedbackCard({
  title,
  subtitle,
  score,
  metrics,
  transcript,
  suggestion,
  feedback,
  onRetry,
  onNext,
}: FeedbackCardProps) {
  return (
    <div className="page narrow-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">SPEAKING FEEDBACK</p>
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </div>
        <div className="score-large">
          {score}
          <span>/100</span>
        </div>
      </div>
      <div className="metric-grid">
        {metrics.map(({ label, score }) => (
          <Card key={label}>
            <strong>{score}</strong>
            <span>{label}</span>
            <ProgressBar value={Number(score.slice(0, -1))} />
          </Card>
        ))}
      </div>
      <Card className="transcript-card">
        <p className="eyebrow">YOUR TRANSCRIPT</p>
        <blockquote>"{transcript}"</blockquote>
        <p className="eyebrow">A MORE NATURAL VERSION</p>
        <blockquote className="corrected">"{suggestion}"</blockquote>
        <div className="usage-tip">
          <strong>AI feedback</strong>
          <p>{feedback}</p>
        </div>
      </Card>
      <div className="complete-actions">
        <Button variant="secondary" onClick={onRetry}>
          Try again
        </Button>
        <Button onClick={onNext}>
          Next question <Icon name="arrow" />
        </Button>
      </div>
    </div>
  );
}
