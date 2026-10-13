import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import type { IconName } from "@/types";

interface QuizCardProps {
  title: string;
  tag: string;
  questions: string;
  duration: string;
  icon: IconName;
  isPrimary?: boolean;
  onStart: () => void;
}

export function QuizCard({
  title,
  tag,
  questions,
  duration,
  icon,
  isPrimary = false,
  onStart,
}: QuizCardProps) {
  return (
    <Card className="quiz-card">
      <span className={`quiz-art art-${isPrimary ? "0" : "1"}`}>
        <Icon name={icon} size="lg" />
      </span>
      <Badge tone={isPrimary ? "primary" : "neutral"}>{tag}</Badge>
      <h2>{title}</h2>
      <p>
        {questions} · {duration}
      </p>
      <Button
        variant={isPrimary ? "primary" : "secondary"}
        onClick={onStart}
      >
        Start quiz <Icon name="arrow" />
      </Button>
    </Card>
  );
}
