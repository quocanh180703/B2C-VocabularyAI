import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";

interface Feature {
  icon: string;
  label: string;
}

interface PackageCardProps {
  name: string;
  price: string;
  duration: string;
  description: string;
  features: Feature[];
  isPopular?: boolean;
  onSelect: () => void;
}

export function PackageCard({
  name,
  price,
  duration,
  description,
  features,
  isPopular = false,
  onSelect,
}: PackageCardProps) {
  return (
    <Card className={`pricing-card ${isPopular ? "popular" : ""}`}>
      {isPopular && <Badge tone="primary">MOST POPULAR</Badge>}
      <div className="pricing-header">
        <h2>{name}</h2>
        <p className="price">
          {price}
          <span>/{duration}</span>
        </p>
        <p className="description">{description}</p>
      </div>
      <ul className="features-list">
        {features.map((feature) => (
          <li key={feature.label}>
            <Icon name="check" size="sm" />
            {feature.label}
          </li>
        ))}
      </ul>
      <Button
        variant={isPopular ? "primary" : "secondary"}
        onClick={onSelect}
        className="pricing-button"
      >
        Choose {name}
      </Button>
    </Card>
  );
}
