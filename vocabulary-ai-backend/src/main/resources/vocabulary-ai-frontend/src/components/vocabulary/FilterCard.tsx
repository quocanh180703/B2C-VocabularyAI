import { Card } from "@/components/common/Card";
import { Icon } from "@/components/common/Icon";

interface FilterCardProps {
  query: string;
  onQueryChange: (value: string) => void;
  level: string;
  onLevelChange: (value: string) => void;
}

export function FilterCard({
  query,
  onQueryChange,
  level,
  onLevelChange,
}: FilterCardProps) {
  const levels = ["All", "A1", "A2", "B1", "B2", "C1"];

  return (
    <Card className="filter-card">
      <label className="search-field">
        <Icon name="search" />
        <input
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder="Search by word or meaning..."
        />
      </label>
      <div className="filter-row">
        <span>CEFR Level</span>
        {levels.map((item) => (
          <button
            key={item}
            className={`filter-chip ${level === item ? "active" : ""}`}
            onClick={() => onLevelChange(item)}
          >
            {item}
          </button>
        ))}
      </div>
    </Card>
  );
}
