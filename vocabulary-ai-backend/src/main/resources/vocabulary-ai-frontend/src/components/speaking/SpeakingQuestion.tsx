import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface SpeakingQuestionProps {
  currentQuestion: number;
  totalQuestions: number;
  level: string;
  question: string;
  onListenQuestion: () => void;
}

export function SpeakingQuestion({
  currentQuestion,
  totalQuestions,
  level,
  question,
  onListenQuestion,
}: SpeakingQuestionProps) {
  return (
    <Card className="speaking-card">
      <div className="topic-row">
        <div>
          <Badge tone="primary">JOB INTERVIEW</Badge>
          <Badge>{level}</Badge>
        </div>
        <span>
          Question {currentQuestion} of {totalQuestions}
        </span>
      </div>
      <div className="speaking-question">
        <p>YOUR QUESTION</p>
        <h2>"{question}"</h2>
        <Button variant="ghost" icon="volume" onClick={onListenQuestion}>
          Listen to question
        </Button>
      </div>
    </Card>
  );
}
