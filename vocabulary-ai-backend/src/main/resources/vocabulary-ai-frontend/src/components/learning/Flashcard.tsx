import { Badge } from "@/components/common/Badge";
import { Icon } from "@/components/common/Icon";

interface FlashcardProps {
  revealed: boolean;
  word: string;
  phonetic: string;
  level: string;
  translation: string;
  meaning: string;
  example: string;
  similarWords: string[];
  onClick: () => void;
}

export function Flashcard({
  revealed,
  word,
  phonetic,
  level,
  translation,
  meaning,
  example,
  similarWords,
  onClick,
}: FlashcardProps) {
  return (
    <button className={`flashcard ${revealed ? "revealed" : ""}`} onClick={onClick}>
      <span className="flashcard-top">
        <Badge tone="primary">{level}</Badge>
        <span className="audio-circle">
          <Icon name="volume" />
        </span>
      </span>
      <span className="flash-word">{word}</span>
      <span className="flash-phonetic">{phonetic}</span>
      {revealed ? (
        <span className="flash-answer">
          <span className="translation-word">{translation}</span>
          <span className="card-divider" />
          <span className="example-label">EXAMPLE</span>
          <span className="example">
            She decided to <strong>{word}</strong> the project.
          </span>
          <span className="similar">Similar: {similarWords.join(" · ")}</span>
        </span>
      ) : (
        <span className="reveal-hint">
          <Icon name="cards" />
          <span>Tap to reveal meaning</span>
        </span>
      )}
    </button>
  );
}
