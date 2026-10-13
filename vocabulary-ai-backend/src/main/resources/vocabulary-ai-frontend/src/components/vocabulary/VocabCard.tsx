import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface VocabCardProps {
  word: string;
  phonetic: string;
  meaning: string;
  level: string;
  type: string;
  category: string;
  isFavorite?: boolean;
  learningStatus?: string;
  onViewWord: () => void;
  onToggleFavorite?: () => void;
}

export function VocabCard({
  word,
  phonetic,
  meaning,
  level,
  type,
  category,
  isFavorite = false,
  learningStatus,
  onViewWord,
  onToggleFavorite,
}: VocabCardProps) {
  return (
    <Card className="vocab-card">
      <div className="vocab-top">
        <Badge tone={level === "B2" || level === "C1" ? "primary" : "neutral"}>
          {level}
        </Badge>
        <button
          className={`icon-button ${isFavorite ? "favorite" : ""}`}
          aria-label={isFavorite ? "Remove from favorites" : "Add to favorites"}
          onClick={onToggleFavorite}
        >
          <Icon name="star" />
        </button>
      </div>
      <h2>{word}</h2>
      <p className="phonetic">{phonetic}</p>
      <p className="definition">{meaning}</p>
      {learningStatus && (
        <div className="learning-status">
          <Badge tone="info">{learningStatus}</Badge>
        </div>
      )}
      <div className="vocab-footer">
        <span>
          {type} · {category}
        </span>
        <Button variant="ghost" onClick={onViewWord}>
          View word <Icon name="arrow" />
        </Button>
      </div>
    </Card>
  );
}
