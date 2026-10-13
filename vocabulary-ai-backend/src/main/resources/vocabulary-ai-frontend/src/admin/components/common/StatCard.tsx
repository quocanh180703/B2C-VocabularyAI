import type { LucideIcon } from "lucide-react"
import { ArrowDownRight, ArrowUpRight } from "lucide-react"
import { Card } from "./ui"

export default function StatCard({
  label,
  value,
  change,
  icon: Icon,
  tone = "primary",
  note = "vs previous period",
}: {
  label: string
  value: string
  change: string
  icon: LucideIcon
  tone?: "primary" | "success" | "warning" | "info"
  note?: string
}) {
  const colors = {
    primary: "bg-primary-soft text-primary",
    success: "bg-success-soft text-success",
    warning: "bg-warning-soft text-warning",
    info: "bg-info-soft text-info",
  }
  const up = !change.startsWith("-")
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between">
        <div
          className={`flex size-11 items-center justify-center rounded-xl ${colors[tone]}`}
        >
          <Icon className="size-5" />
        </div>
        <div
          className={`flex items-center text-xs font-bold ${
            up ? "text-success" : "text-danger"
          }`}
        >
          {up ? (
            <ArrowUpRight className="size-4" />
          ) : (
            <ArrowDownRight className="size-4" />
          )}
          {change}
        </div>
      </div>
      <div className="mt-5 text-2xl font-extrabold tracking-tight">{value}</div>
      <div className="mt-1 text-sm font-medium text-muted-foreground">
        {label}
      </div>
      <div className="mt-3 text-[11px] text-muted-foreground">{note}</div>
    </Card>
  )
}
