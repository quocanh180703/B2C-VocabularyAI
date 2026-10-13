import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface AnswerZoneProps {
  revealed: boolean;
  feedback: "known" | "again" | null;
  onKnown: () => void;
  onAgain: () => void;
  onReveal: () => void;
}

export function AnswerZone({
  revealed,
  feedback,
  onKnown,
  onAgain,
  onReveal,
}: AnswerZoneProps) {
  if (!revealed) {
    return <Button onClick={onReveal}>Reveal answer</Button>;
  }

  return (
    <div className="answer-zone">
      <p>How well did you remember?</p>
      <div>
        <Button variant="danger" icon="close" onClick={onAgain}>
          Didn't know
        </Button>
        <Button variant="success" icon="check" onClick={onKnown}>
          Knew it
        </Button>
      </div>
      {feedback && (
        <span className={`feedback ${feedback}`}>
          {feedback === "known"
            ? "Great! We'll increase the review interval."
            : "We'll bring this word back again soon."}
        </span>
      )}
    </div>
  );
}
