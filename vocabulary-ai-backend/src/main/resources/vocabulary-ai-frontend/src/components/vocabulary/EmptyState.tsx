import { Card } from "@/components/common/Card";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface EmptyStateProps {
  onClearFilters: () => void;
}

export function EmptyState({ onClearFilters }: EmptyStateProps) {
  return (
    <Card className="empty-state">
      <span>
        <Icon name="search" size="lg" />
      </span>
      <h2>No words found</h2>
      <p>Try changing your search or level filters.</p>
      <Button onClick={onClearFilters}>Clear filters</Button>
    </Card>
  );
}
