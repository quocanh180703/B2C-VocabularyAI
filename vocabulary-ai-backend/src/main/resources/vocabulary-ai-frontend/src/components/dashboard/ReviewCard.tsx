import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface ReviewCardProps {
  onStartReview: () => void;
}

export function ReviewCard({ onStartReview }: ReviewCardProps) {
  return (
    <Card className="review-card">
      <div className="review-copy">
        <Badge tone="primary">TODAY'S REVIEW</Badge>
        <h2>
          Build momentum with
          <br />a quick review
        </h2>
        <p>12 words are ready. Most learners finish this set in about 8 minutes.</p>
        <Button icon="cards" onClick={onStartReview}>
          Start review
        </Button>
      </div>
      <div className="review-orbit" aria-hidden="true">
        <div className="orbit-card orbit-one">achieve</div>
        <div className="orbit-card orbit-two">accurate</div>
        <div className="orbit-card orbit-main">
          <Icon name="brain" size="lg" />
          <strong>12</strong>
          <span>words ready</span>
        </div>
      </div>
    </Card>
  );
}
