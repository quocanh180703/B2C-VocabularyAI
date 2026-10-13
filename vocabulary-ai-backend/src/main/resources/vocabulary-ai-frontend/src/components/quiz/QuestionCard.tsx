import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";
import { formatTimeTaken } from "@/constants/quiz";

interface QuestionCardProps {
  current: number;
  total: number;
  question: string;
  options: string[];
  selectedAnswer: number | null;
  onSelectAnswer: (index: number) => void;
}

export function QuestionCard({
  current,
  total,
  question,
  options,
  selectedAnswer,
  onSelectAnswer,
}: QuestionCardProps) {
  const progress = (current / total) * 100;

  return (
    <>
      <div className="quiz-head">
        <div>
          <p className="eyebrow">QUIZ</p>
          <h1>
            Question {current} of {total}
          </h1>
        </div>
        <div className="timer">
          <Icon name="clock" />
          {formatTimeTaken(Math.floor(Math.random() * 600))}
        </div>
      </div>
      <ProgressBar value={progress} />
      <Card className="question-card">
        <Badge tone="primary">CHOOSE ONE ANSWER</Badge>
        <h2>{question}</h2>
        <div className="answers">
          {options.map((option, index) => (
            <button
              key={option}
              onClick={() => onSelectAnswer(index)}
              className={selectedAnswer === index ? "selected" : ""}
            >
              <span>{String.fromCharCode(65 + index)}</span>
              {option}
            </button>
          ))}
        </div>
      </Card>
    </>
  );
}
