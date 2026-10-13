import { ReactNode } from "react"

interface FilterBarProps {
  children?: ReactNode
}

export function FilterBar({ children }: FilterBarProps) {
  return (
    <div className="grid gap-3 p-5 md:grid-cols-auto md:gap-2 flex-wrap">
      {children}
    </div>
  )
}
