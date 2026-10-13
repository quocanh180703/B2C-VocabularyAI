import type { ReactNode } from "react"

export default function PageHeader({
  title,
  description,
  actions,
}: {
  title: string
  description: string
  actions?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div>
        <div className="text-2xl font-extrabold tracking-tight text-foreground">
          {title}
        </div>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
      {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
    </div>
  )
}
