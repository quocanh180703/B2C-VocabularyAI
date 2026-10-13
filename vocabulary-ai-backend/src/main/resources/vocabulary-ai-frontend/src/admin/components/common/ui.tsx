import type {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from "react"
import { ChevronLeft, ChevronRight, Search, X } from "lucide-react"

export function Button({
  children,
  className = "",
  variant = "primary",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost" | "danger"
}) {
  const variants = {
    primary: "bg-primary text-white hover:bg-primary-hover shadow-sm",
    secondary:
      "border border-border bg-card text-card-foreground hover:bg-muted",
    ghost: "text-muted-foreground hover:bg-muted hover:text-foreground",
    danger: "bg-danger-soft text-danger hover:opacity-80",
  }
  return (
    <button
      className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-xl px-4 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-primary/25 disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}

export function Input({
  className = "",
  ...props
}: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={`min-h-10 w-full rounded-xl border border-border bg-card px-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-primary/10 ${className}`}
      {...props}
    />
  )
}

export function Textarea({
  className = "",
  ...props
}: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={`min-h-24 w-full resize-y rounded-xl border border-border bg-card px-3 py-2 text-sm text-foreground outline-none transition placeholder:text-muted-foreground focus:border-primary focus:ring-2 focus:ring-primary/10 ${className}`}
      {...props}
    />
  )
}

export function SearchInput(props: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <div className="relative">
      <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
      <Input className="pl-9" {...props} />
    </div>
  )
}

export function Select({
  className = "",
  children,
  ...props
}: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={`min-h-10 rounded-xl border border-border bg-card px-3 text-sm text-foreground outline-none focus:border-primary ${className}`}
      {...props}
    >
      {children}
    </select>
  )
}

export function Card({
  children,
  className = "",
}: {
  children: ReactNode
  className?: string
}) {
  return (
    <div
      className={`rounded-2xl border border-border bg-card text-card-foreground shadow-sm shadow-black/[0.025] ${className}`}
    >
      {children}
    </div>
  )
}

export function Badge({
  children,
  tone = "neutral",
}: {
  children: ReactNode
  tone?: "success" | "warning" | "danger" | "info" | "neutral"
}) {
  const tones = {
    success: "bg-success-soft text-success",
    warning: "bg-warning-soft text-warning",
    danger: "bg-danger-soft text-danger",
    info: "bg-info-soft text-info",
    neutral: "bg-muted text-muted-foreground",
  }
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${tones[tone]}`}
    >
      {children}
    </span>
  )
}

export function Modal({
  open,
  onClose,
  title,
  children,
  footer,
  wide = false,
}: {
  open: boolean
  onClose: () => void
  title: string
  children: ReactNode
  footer?: ReactNode
  wide?: boolean
}) {
  if (!open) return null
  return (
    <div
      className="fixed inset-0 z-[80] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
      onMouseDown={onClose}
    >
      <div
        className={`max-h-[92vh] w-full overflow-auto rounded-2xl border border-border bg-card shadow-2xl ${
          wide ? "max-w-3xl" : "max-w-lg"
        }`}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-border p-5">
          <div className="text-lg font-bold">{title}</div>
          <Button
            variant="ghost"
            className="size-10 px-0"
            onClick={onClose}
            aria-label="Close modal"
          >
            <X className="size-5" />
          </Button>
        </div>
        <div className="p-5">{children}</div>
        {footer && (
          <div className="flex justify-end gap-3 border-t border-border p-5">
            {footer}
          </div>
        )}
      </div>
    </div>
  )
}

export function Pagination({
  page,
  pages,
  onChange,
}: {
  page: number
  pages: number
  onChange: (page: number) => void
}) {
  return (
    <div className="flex items-center justify-between gap-3 border-t border-border px-5 py-4">
      <span className="text-sm text-muted-foreground">
        Page {page} of {pages}
      </span>
      <div className="flex gap-2">
        <Button
          variant="secondary"
          className="size-9 min-h-9 px-0"
          disabled={page === 1}
          onClick={() => onChange(page - 1)}
          aria-label="Previous page"
        >
          <ChevronLeft className="size-4" />
        </Button>
        <Button
          variant="secondary"
          className="size-9 min-h-9 px-0"
          disabled={page === pages}
          onClick={() => onChange(page + 1)}
          aria-label="Next page"
        >
          <ChevronRight className="size-4" />
        </Button>
      </div>
    </div>
  )
}

export function EmptyState({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <div className="p-12 text-center">
      <div className="font-semibold">{title}</div>
      <p className="mt-1 text-sm text-muted-foreground">{description}</p>
    </div>
  )
}

export function LoadingState() {
  return (
    <div className="space-y-3 p-6">
      {[1, 2, 3, 4].map((item) => (
        <div key={item} className="h-12 animate-pulse rounded-xl bg-muted" />
      ))}
    </div>
  )
}

export const formatVnd = (value: number) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value)
