import { useMemo, useState } from "react"
import {
  Bell,
  BookOpen,
  ChevronDown,
  CreditCard,
  GraduationCap,
  Menu,
  Moon,
  Search,
  Sun,
} from "lucide-react"
import { useLocation, useNavigate } from "react-router"
import { studentsSeed } from "../../data/students"
import { vocabularySeed } from "../../data/vocabulary"
import { useAppStore } from "../../store/useAppStore"
import { Button, Input } from "../common/ui"
import { allNavigation } from "./navigation"

export default function Header() {
  const { theme, setTheme, setMobileOpen, user } = useAppStore()
  const [query, setQuery] = useState("")
  const [profileOpen, setProfileOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const title =
    allNavigation.find((item) => location.pathname.startsWith(item.to))
      ?.label ?? "Dashboard"
  const results = useMemo(() => {
    if (query.trim().length < 2) return []
    const term = query.toLowerCase()
    return [
      ...studentsSeed
        .filter((item) => item.name.toLowerCase().includes(term))
        .slice(0, 2)
        .map((item) => ({
          label: item.name,
          meta: "Student",
          icon: GraduationCap,
          to: `/admin/students/${item.id}`,
        })),
      ...vocabularySeed
        .filter((item) => item.word.includes(term))
        .slice(0, 2)
        .map((item) => ({
          label: item.word,
          meta: `Vocabulary · ${item.cefr}`,
          icon: BookOpen,
          to: `/admin/vocabulary/${item.id}`,
        })),
      ...(term.includes("payment")
        ? [
            {
              label: "Recent PayOS payments",
              meta: "Payments",
              icon: CreditCard,
              to: "/admin/payments",
            },
          ]
        : []),
    ]
  }, [query])
  const dark =
    theme === "dark" ||
    (theme === "system" &&
      window.matchMedia("(prefers-color-scheme: dark)").matches)
  return (
    <header className="sticky top-0 z-30 flex h-20 items-center gap-3 border-b border-border bg-card/95 px-4 backdrop-blur-xl sm:px-6">
      <Button
        variant="ghost"
        className="size-10 px-0 lg:hidden"
        onClick={() => setMobileOpen(true)}
        aria-label="Open navigation"
      >
        <Menu className="size-5" />
      </Button>
      <div className="hidden text-lg font-bold sm:block">{title}</div>
      <div className="relative ml-auto hidden w-full max-w-md md:block">
        <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          className="bg-muted pl-9"
          placeholder="Search students, vocabulary, payments..."
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        {results.length > 0 && (
          <div className="absolute top-12 w-full rounded-xl border border-border bg-card p-2 shadow-xl">
            {results.map(({ label, meta, icon: Icon, to }) => (
              <Button
                key={`${meta}-${label}`}
                variant="ghost"
                className="h-auto w-full justify-start px-3 py-2.5"
                onClick={() => {
                  navigate(to)
                  setQuery("")
                }}
              >
                <span className="flex size-8 items-center justify-center rounded-lg bg-primary-soft text-primary">
                  <Icon className="size-4" />
                </span>
                <span className="text-left">
                  <b className="block text-sm">{label}</b>
                  <span className="text-xs font-normal text-muted-foreground">
                    {meta}
                  </span>
                </span>
              </Button>
            ))}
          </div>
        )}
      </div>
      <Button
        variant="ghost"
        className="size-10 px-0"
        onClick={() => setTheme(dark ? "light" : "dark")}
        aria-label="Toggle theme"
      >
        {dark ? <Sun className="size-5" /> : <Moon className="size-5" />}
      </Button>
      <Button
        variant="ghost"
        className="relative size-10 px-0"
        onClick={() => navigate("/admin/notifications")}
        aria-label="Notifications"
      >
        <Bell className="size-5" />
        <span className="absolute right-2 top-2 size-2 rounded-full bg-danger ring-2 ring-card" />
      </Button>
      <div className="relative">
        <Button
          variant="ghost"
          className="gap-2 px-2"
          onClick={() => setProfileOpen(!profileOpen)}
        >
          <span className="flex size-8 items-center justify-center rounded-full bg-primary text-xs font-bold text-white">
            {user?.avatar}
          </span>
          <span className="hidden text-left text-xs sm:block">
            <b className="block">{user?.name}</b>
            <span className="text-muted-foreground">Administrator</span>
          </span>
          <ChevronDown className="hidden size-3 sm:block" />
        </Button>
        {profileOpen && (
          <div className="absolute right-0 top-12 w-48 rounded-xl border border-border bg-card p-2 shadow-xl">
            <Button
              variant="ghost"
              className="w-full justify-start"
              onClick={() => {
                navigate("/admin/settings")
                setProfileOpen(false)
              }}
            >
              Profile settings
            </Button>
            <Button
              variant="ghost"
              className="w-full justify-start"
              onClick={() => {
                navigate("/admin/admins")
                setProfileOpen(false)
              }}
            >
              Administrators
            </Button>
          </div>
        )}
      </div>
    </header>
  )
}
