import {
  Bell,
  BrainCircuit,
  CreditCard,
  GraduationCap,
  Trash2,
} from "lucide-react"
import { useMemo, useState } from "react"
import PageHeader from "../components/common/PageHeader"
import { Button, Card, EmptyState, Select } from "../components/common/ui"
import { useAppStore } from "../store/useAppStore"

const initial = [
  {
    id: 1,
    type: "AI Service",
    title: "Gemini latency returned to normal",
    description: "Average response time is now below 1.5 seconds.",
    time: "8 min ago",
    read: false,
    icon: BrainCircuit,
  },
  {
    id: 2,
    type: "Payment",
    title: "Payment reconciliation completed",
    description: "1,842 successful PayOS transactions were reconciled.",
    time: "32 min ago",
    read: false,
    icon: CreditCard,
  },
  {
    id: 3,
    type: "User Activity",
    title: "Student milestone reached",
    description: "VocabularyAI now has 12,450 registered students.",
    time: "2 hours ago",
    read: false,
    icon: GraduationCap,
  },
  {
    id: 4,
    type: "System",
    title: "Weekly backup completed",
    description: "Database and prompt configuration backups are available.",
    time: "Yesterday",
    read: true,
    icon: Bell,
  },
]
export default function NotificationsPage() {
  const [items, setItems] = useState(initial)
  const [type, setType] = useState("All")
  const notify = useAppStore((state) => state.notify)
  const filtered = useMemo(
    () => items.filter((item) => type === "All" || item.type === type),
    [items, type],
  )
  const markAll = () => {
    setItems(items.map((item) => ({ ...item, read: true })))
    notify("All notifications marked as read.")
  }
  return (
    <>
      <PageHeader
        title="Notifications"
        description="System, payment, AI service and student activity updates."
        actions={
          <>
            <Select
              value={type}
              onChange={(event) => setType(event.target.value)}
            >
              <option>All</option>
              <option>System</option>
              <option>Payment</option>
              <option>AI Service</option>
              <option>User Activity</option>
            </Select>
            <Button onClick={markAll}>Mark all as read</Button>
          </>
        }
      />
      <Card className="overflow-hidden">
        {filtered.length ? (
          <div className="divide-y divide-border">
            {filtered.map(
              ({ id, title, description, time, read, icon: Icon }) => (
                <div
                  key={id}
                  className={`flex gap-4 p-5 ${
                    read ? "" : "bg-primary-soft/40"
                  }`}
                >
                  <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary-soft text-primary">
                    <Icon className="size-5" />
                  </div>
                  <Button
                    variant="ghost"
                    className="h-auto min-w-0 flex-1 justify-start p-0 text-left"
                    onClick={() =>
                      setItems(
                        items.map((item) =>
                          item.id === id ? { ...item, read: true } : item,
                        ),
                      )
                    }
                  >
                    <span>
                      <b className="block text-sm">{title}</b>
                      <span className="mt-1 block text-sm font-normal text-muted-foreground">
                        {description}
                      </span>
                      <span className="mt-2 block text-xs font-normal text-muted-foreground">
                        {time}
                      </span>
                    </span>
                  </Button>
                  {!read && (
                    <span className="mt-2 size-2 shrink-0 rounded-full bg-primary" />
                  )}
                  <Button
                    variant="ghost"
                    className="size-9 min-h-9 shrink-0 px-0 text-danger"
                    onClick={() => {
                      setItems(items.filter((item) => item.id !== id))
                      notify("Notification deleted.", "warning")
                    }}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              ),
            )}
          </div>
        ) : (
          <EmptyState
            title="No notifications"
            description="You are all caught up."
          />
        )}
      </Card>
    </>
  )
}
