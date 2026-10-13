import { AlertTriangle, CheckCircle2, Info, X, XCircle } from "lucide-react"
import { useAppStore } from "../../store/useAppStore"
import { Button } from "./ui"

export default function ToastContainer() {
  const { toasts, dismissToast } = useAppStore()
  return (
    <div className="fixed bottom-5 right-5 z-[90] flex w-[calc(100%-2.5rem)] max-w-sm flex-col gap-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className="flex items-center gap-3 rounded-xl border border-border bg-card p-4 text-sm font-medium text-foreground shadow-xl"
        >
          {toast.tone === "success" && (
            <CheckCircle2 className="size-5 text-success" />
          )}
          {toast.tone === "error" && <XCircle className="size-5 text-danger" />}
          {toast.tone === "warning" && (
            <AlertTriangle className="size-5 text-warning" />
          )}
          {toast.tone === "info" && <Info className="size-5 text-info" />}
          <span className="flex-1">{toast.message}</span>
          <Button
            variant="ghost"
            className="size-8 min-h-8 px-0"
            onClick={() => dismissToast(toast.id)}
            aria-label="Dismiss notification"
          >
            <X className="size-4" />
          </Button>
        </div>
      ))}
    </div>
  )
}
