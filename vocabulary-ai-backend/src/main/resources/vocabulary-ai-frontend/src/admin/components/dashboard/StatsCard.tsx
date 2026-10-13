import { ReactNode } from "react"
import { Card } from "../common/ui"

interface StatsCardProps {
  title: string
  value: string | number
  description?: string
  icon?: ReactNode
  trend?: {
    value: number
    label: string
    up?: boolean
  }
}

export function StatsCard({
  title,
  value,
  description,
  icon,
  trend,
}: StatsCardProps) {
  return (
    <Card className="p-6">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="mt-2 text-3xl font-bold">{value}</p>
          {description && (
            <p className="mt-2 text-xs text-muted-foreground">{description}</p>
          )}
          {trend && (
            <p
              className={`mt-2 text-xs font-medium ${
                trend.up ? "text-green-600" : "text-red-600"
              }`}
            >
              {trend.up ? "↑" : "↓"} {trend.value}% {trend.label}
            </p>
          )}
        </div>
        {icon && (
          <div className="text-muted-foreground opacity-40">
            {icon}
          </div>
        )}
      </div>
    </Card>
  )
}
