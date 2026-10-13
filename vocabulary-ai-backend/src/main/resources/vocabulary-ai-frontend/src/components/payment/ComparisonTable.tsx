import { Card } from "@/components/common/Card";
import { Icon } from "@/components/common/Icon";

interface ComparisonFeature {
  name: string;
  free: boolean | string;
  premium: boolean | string;
}

interface ComparisonTableProps {
  features: ComparisonFeature[];
}

export function ComparisonTable({ features }: ComparisonTableProps) {
  return (
    <Card className="comparison-table">
      <table>
        <thead>
          <tr>
            <th>Feature</th>
            <th>Free</th>
            <th>Premium</th>
          </tr>
        </thead>
        <tbody>
          {features.map((feature) => (
            <tr key={feature.name}>
              <td>{feature.name}</td>
              <td>
                {typeof feature.free === "boolean" ? (
                  feature.free ? (
                    <Icon name="check" />
                  ) : (
                    <Icon name="close" />
                  )
                ) : (
                  feature.free
                )}
              </td>
              <td>
                {typeof feature.premium === "boolean" ? (
                  feature.premium ? (
                    <Icon name="check" />
                  ) : (
                    <Icon name="close" />
                  )
                ) : (
                  feature.premium
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </Card>
  );
}
