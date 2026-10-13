import { ReactNode } from "react"

export interface InfoItem {
  label: string
  value: string | number | ReactNode
  mono?: boolean
  bold?: boolean
}

interface InfoGridProps {
  items: InfoItem[]
  cols?: number
}

export function InfoGrid({ items, cols = 2 }: InfoGridProps) {
  const colsClass = {
    1: "md:grid-cols-1",
    2: "md:grid-cols-2",
    3: "md:grid-cols-3",
    4: "md:grid-cols-4",
  }[cols] || "md:grid-cols-2"

  return (
    <div className={`grid gap-4 ${colsClass}`}>
      {items.map((item, idx) => (
        <div key={idx} className="space-y-1">
          <p className="text-xs font-medium text-muted-foreground uppercase">
            {item.label}
          </p>
          <p
            className={`text-lg ${item.bold ? "font-bold" : "font-semibold"} ${
              item.mono ? "font-mono" : ""
            }`}
          >
            {item.value}
          </p>
        </div>
      ))}
    </div>
  )
}
