import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface InsightCardProps {
  quote: string;
  tags: string[];
  onReview: () => void;
}

export function InsightCard({ quote, tags, onReview }: InsightCardProps) {
  return (
    <Card className="insight-card">
      <div className="insight-heading">
        <span>
          <Icon name="spark" />
        </span>
        <div>
          <h3>AI learning insight</h3>
          <p>Based on your last 7 days</p>
        </div>
      </div>
      <p className="insight-quote">{quote}</p>
      <div className="insight-tags">
        {tags.map((tag) => (
          <Badge key={tag} tone={tag.includes("Business") ? "warning" : "neutral"}>
            {tag}
          </Badge>
        ))}
      </div>
      <Button variant="secondary" onClick={onReview}>
        Review 8 recommended words <Icon name="arrow" />
      </Button>
    </Card>
  );
}
