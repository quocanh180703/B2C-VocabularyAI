import type { ReactNode } from "react"
import { Card } from "./ui"

export default function ChartCard({
  title,
  description,
  action,
  children,
  className = "",
}: {
  title: string
  description: string
  action?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <Card className={`p-5 ${className}`}>
      <div className="mb-5 flex items-start justify-between gap-3">
        <div>
          <div className="font-bold">{title}</div>
          <div className="mt-1 text-sm text-muted-foreground">
            {description}
          </div>
        </div>
        {action}
      </div>
      {children}
    </Card>
  )
}
