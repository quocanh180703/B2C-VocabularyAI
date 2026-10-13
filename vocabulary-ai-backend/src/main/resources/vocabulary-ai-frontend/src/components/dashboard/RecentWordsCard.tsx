import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { Word } from "@/types";

interface RecentWordsCardProps {
  words: Word[];
  onViewAll: () => void;
}

export function RecentWordsCard({ words, onViewAll }: RecentWordsCardProps) {
  return (
    <Card>
      <div className="section-title">
        <div>
          <h3>Recently learned</h3>
          <p>Keep these words fresh</p>
        </div>
        <Button variant="ghost" onClick={onViewAll}>
          View all <Icon name="arrow" />
        </Button>
      </div>
      <div className="word-list">
        {words.map((word, index) => (
          <div className="word-row" key={word.word}>
            <span className="word-number">0{index + 1}</span>
            <div>
              <strong>{word.word}</strong>
              <p>{word.phonetic}</p>
            </div>
            <Badge tone={index === 2 ? "primary" : "neutral"}>{word.level}</Badge>
            <span className="word-type">{word.type}</span>
            <Button variant="ghost" icon="volume">
              <span className="sr-only">Play audio</span>
            </Button>
          </div>
        ))}
      </div>
    </Card>
  );
}
