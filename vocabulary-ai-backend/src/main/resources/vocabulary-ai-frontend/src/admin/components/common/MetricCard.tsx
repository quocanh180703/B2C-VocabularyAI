import { ReactNode } from "react"
import { Card } from "./ui"

interface MetricCardProps {
  title: string
  value: string | number
  subtitle?: string
  icon?: ReactNode
  action?: ReactNode
  highlighted?: boolean
}

export function MetricCard({
  title,
  value,
  subtitle,
  icon,
  action,
  highlighted = false,
}: MetricCardProps) {
  return (
    <Card
      className={`p-6 ${
        highlighted ? "border-blue-200 bg-blue-50 dark:border-blue-900 dark:bg-blue-950" : ""
      }`}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="mt-2 text-3xl font-bold">{value}</p>
          {subtitle && (
            <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
          )}
        </div>
        <div className="flex flex-col items-end gap-2">
          {icon && <div className="text-muted-foreground opacity-40">{icon}</div>}
          {action && <div className="text-xs">{action}</div>}
        </div>
      </div>
    </Card>
  )
}
