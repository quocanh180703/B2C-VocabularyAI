import {
  BarChart3,
  Bell,
  BookOpen,
  BrainCircuit,
  ChartNoAxesCombined,
  CircleDollarSign,
  ClipboardCheck,
  CreditCard,
  FileBarChart,
  FilePenLine,
  Gauge,
  GraduationCap,
  Layers3,
  MessageSquareText,
  Mic2,
  Package,
  Settings,
  ShieldCheck,
  Sparkles,
  UserCog,
  Users,
  WalletCards,
} from "lucide-react"

export const navigation = [
  {
    group: "Overview",
    items: [{ to: "/admin/dashboard", label: "Dashboard", icon: Gauge }],
  },
  {
    group: "Learning Management",
    items: [
      { to: "/admin/vocabulary", label: "Vocabulary", icon: BookOpen },
      { to: "/admin/cefr-levels", label: "CEFR Levels", icon: Layers3 },
      {
        to: "/admin/learning-progress",
        label: "Learning Progress",
        icon: ChartNoAxesCombined,
      },
      { to: "/admin/quiz", label: "Quiz", icon: ClipboardCheck },
      { to: "/admin/writing", label: "Writing", icon: FilePenLine },
      { to: "/admin/speaking", label: "Speaking", icon: Mic2 },
    ],
  },
  {
    group: "AI Management",
    items: [
      { to: "/admin/ai-usage", label: "AI Usage", icon: Sparkles },
      { to: "/admin/ai-prompts", label: "AI Prompts", icon: MessageSquareText },
      { to: "/admin/ai-analytics", label: "AI Analytics", icon: BrainCircuit },
    ],
  },
  {
    group: "User Management",
    items: [
      { to: "/admin/students", label: "Students", icon: GraduationCap },
      { to: "/admin/admins", label: "Administrators", icon: UserCog },
    ],
  },
  {
    group: "Business",
    items: [
      { to: "/admin/packages", label: "Packages", icon: Package },
      { to: "/admin/subscriptions", label: "Subscriptions", icon: WalletCards },
      { to: "/admin/payments", label: "Payments", icon: CreditCard },
    ],
  },
  {
    group: "System",
    items: [
      { to: "/admin/reports", label: "Reports", icon: FileBarChart },
      { to: "/admin/notifications", label: "Notifications", icon: Bell },
      { to: "/admin/settings", label: "Settings", icon: Settings },
    ],
  },
]

export const allNavigation = navigation.flatMap((section) => section.items)
export const brandIcon = BrainCircuit
export const adminIcon = ShieldCheck
export const analyticsIcon = BarChart3
export const revenueIcon = CircleDollarSign
export const usersIcon = Users
