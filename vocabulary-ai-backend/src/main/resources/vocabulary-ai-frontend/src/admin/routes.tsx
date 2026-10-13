import { Navigate, createBrowserRouter, useParams } from "react-router"
import AdminLayout from "./components/layout/AdminLayout"
import { ForbiddenPage, LoginPage, ProtectedRoute } from "./pages/AuthPages"

const domain = async () => import("./pages/DomainPages")
const aliases = [
  "dashboard",
  "vocabulary",
  "cefr-levels",
  "learning-progress",
  "quiz",
  "writing",
  "speaking",
  "ai-usage",
  "ai-prompts",
  "ai-analytics",
  "students",
  "admins",
  "packages",
  "subscriptions",
  "payments",
  "reports",
  "notifications",
  "settings",
]

function DetailRedirect({
  section,
}: {
  section: "vocabulary" | "quiz" | "students"
}) {
  const { id } = useParams()
  return <Navigate to={`/admin/${section}/${id ?? ""}`} replace />
}

export const router = createBrowserRouter([
  { path: "/login", Component: LoginPage },
  { path: "/403", Component: ForbiddenPage },
  {
    Component: ProtectedRoute,
    children: [
      {
        path: "/admin",
        Component: AdminLayout,
        children: [
          { index: true, element: <Navigate to="/admin/dashboard" replace /> },
          {
            path: "dashboard",
            lazy: async () => ({
              Component: (await import("./pages/AdminSystemPage")).default,
            }),
          },
          {
            path: "vocabulary",
            lazy: async () => ({
              Component: (await import("./pages/VocabularyPage")).default,
            }),
          },
          {
            path: "vocabulary/:id",
            lazy: async () => ({
              Component: (await import("./pages/VocabularyPage"))
                .VocabularyDetailPage,
            }),
          },
          {
            path: "cefr-levels",
            lazy: async () => ({
              Component: (await import("./pages/CefrLevelsPage")).default,
            }),
          },
          {
            path: "learning-progress",
            lazy: async () => ({
              Component: (await domain()).LearningProgressPage,
            }),
          },
          {
            path: "quiz",
            lazy: async () => ({
              Component: (await import("./pages/QuizPage")).default,
            }),
          },
          {
            path: "quiz/:id",
            lazy: async () => ({
              Component: (await import("./pages/QuizPage")).QuizDetailPage,
            }),
          },
          {
            path: "writing",
            lazy: async () => ({ Component: (await domain()).WritingPage }),
          },
          {
            path: "speaking",
            lazy: async () => ({
              Component: (await import("./pages/SpeakingPage")).default,
            }),
          },
          {
            path: "speaking/:id",
            lazy: async () => ({
              Component: (await import("./pages/SpeakingPage"))
                .SpeakingDetailPage,
            }),
          },
          {
            path: "ai",
            lazy: async () => ({
              Component: (await import("./pages/AiConversationsPage")).default,
            }),
          },
          {
            path: "ai/:id",
            lazy: async () => ({
              Component: (await import("./pages/AiConversationsPage"))
                .AiConversationDetailPage,
            }),
          },
          {
            path: "ai-usage",
            lazy: async () => ({ Component: (await domain()).AiUsagePage }),
          },
          {
            path: "ai-prompts",
            lazy: async () => ({ Component: (await domain()).AiPromptsPage }),
          },
          {
            path: "ai-analytics",
            lazy: async () => ({ Component: (await domain()).AiAnalyticsPage }),
          },
          {
            path: "students",
            lazy: async () => ({
              Component: (await import("./pages/StudentsPage")).default,
            }),
          },
          {
            path: "students/:id",
            lazy: async () => ({
              Component: (await import("./pages/StudentsPage"))
                .StudentDetailPage,
            }),
          },
          {
            path: "admins",
            lazy: async () => ({ Component: (await domain()).AdminsPage }),
          },
          {
            path: "packages",
            lazy: async () => ({
              Component: (await import("./pages/PackagesPage")).default,
            }),
          },
          {
            path: "packages/:id",
            lazy: async () => ({
              Component: (await import("./pages/PackagesPage")).PackageDetailPage,
            }),
          },
          {
            path: "subscriptions",
            lazy: async () => ({
              Component: (await domain()).SubscriptionsPage,
            }),
          },
          {
            path: "payments",
            lazy: async () => ({ Component: (await domain()).PaymentsPage }),
          },
          {
            path: "reports",
            lazy: async () => ({
              Component: (await import("./pages/ReportsPage")).default,
            }),
          },
          {
            path: "notifications",
            lazy: async () => ({
              Component: (await import("./pages/NotificationsPage")).default,
            }),
          },
          {
            path: "settings",
            lazy: async () => ({
              Component: (await import("./pages/SettingsPage")).default,
            }),
          },
          { path: "*", element: <Navigate to="/admin/dashboard" replace /> },
        ],
      },
    ],
  },
  ...aliases.map((path) => ({
    path: `/${path}`,
    element: <Navigate to={`/admin/${path}`} replace />,
  })),
  {
    path: "/vocabulary/:id",
    element: <DetailRedirect section="vocabulary" />,
  },
  { path: "/quiz/:id", element: <DetailRedirect section="quiz" /> },
  { path: "/students/:id", element: <DetailRedirect section="students" /> },
  { path: "/", element: <Navigate to="/admin/dashboard" replace /> },
  { path: "*", element: <Navigate to="/admin/dashboard" replace /> },
])
