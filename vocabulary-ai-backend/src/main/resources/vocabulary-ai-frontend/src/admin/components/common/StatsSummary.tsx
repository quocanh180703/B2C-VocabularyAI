import { ReactNode } from "react"
import { Card } from "./ui"

interface StatItem {
  label: string
  value: string | number
  icon?: ReactNode
  color?: "success" | "warning" | "error" | "info"
}

interface StatsSummaryProps {
  items: StatItem[]
  cols?: number
}

const colorClasses = {
  success: "text-green-600",
  warning: "text-yellow-600",
  error: "text-red-600",
  info: "text-blue-600",
}

export function StatsSummary({ items, cols = 4 }: StatsSummaryProps) {
  const colsClass = {
    1: "md:grid-cols-1",
    2: "md:grid-cols-2",
    3: "md:grid-cols-3",
    4: "md:grid-cols-4",
  }[cols] || "md:grid-cols-4"

  return (
    <div className={`grid gap-4 ${colsClass}`}>
      {items.map((item, idx) => (
        <Card key={idx} className="p-4">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-xs font-medium text-muted-foreground uppercase">
                {item.label}
              </p>
              <p
                className={`mt-2 text-2xl font-bold ${
                  item.color ? colorClasses[item.color] : ""
                }`}
              >
                {item.value}
              </p>
            </div>
            {item.icon && (
              <div className="text-muted-foreground opacity-40">
                {item.icon}
              </div>
            )}
          </div>
        </Card>
      ))}
    </div>
  )
}
