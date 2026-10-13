import { create } from "zustand"
import type { AdminUser } from "../types"
import type {
  DashboardSummary,
  DashboardRevenue,
  TopStudent,
  ActivityTrend,
} from "../services/dashboardService"
import type {
  StudentListItem,
  StudentDetail,
  StudentStats,
  PageResponse,
} from "../services/studentService"
import type {
  VocabularyListItem,
  VocabularyDetail,
  CefrStats,
  Category,
} from "../services/vocabularyService"

type Theme = "light" | "dark" | "system"
type ToastTone = "success" | "error" | "warning" | "info"

interface Toast {
  id: number
  message: string
  tone: ToastTone
}

interface AppState {
  theme: Theme
  sidebarCollapsed: boolean
  mobileOpen: boolean
  isAuthenticated: boolean
  user: AdminUser | null
  toasts: Toast[]
  // Dashboard data
  dashboardSummary: DashboardSummary | null
  dashboardRevenue: DashboardRevenue | null
  topStudents: TopStudent[] | null
  activityTrend: ActivityTrend | null
  dashboardLoading: boolean
  dashboardError: string | null
  // Students data
  studentList: PageResponse<StudentListItem> | null
  studentDetail: StudentDetail | null
  studentStats: StudentStats | null
  studentsLoading: boolean
  studentsError: string | null
  // Vocabulary data
  vocabularyList: PageResponse<VocabularyListItem> | null
  vocabularyDetail: VocabularyDetail | null
  cefrStats: CefrStats | null
  categories: PageResponse<Category> | null
  vocabularyLoading: boolean
  vocabularyError: string | null
  // Methods
  setTheme: (theme: Theme) => void
  toggleSidebar: () => void
  setMobileOpen: (open: boolean) => void
  login: (email: string, password: string) => Promise<boolean>
  logout: () => void
  notify: (message: string, tone?: ToastTone) => void
  dismissToast: (id: number) => void
  // Dashboard methods
  setDashboardSummary: (summary: DashboardSummary) => void
  setDashboardRevenue: (revenue: DashboardRevenue) => void
  setTopStudents: (students: TopStudent[]) => void
  setActivityTrend: (trend: ActivityTrend) => void
  setDashboardLoading: (loading: boolean) => void
  setDashboardError: (error: string | null) => void
  // Students methods
  setStudentList: (list: PageResponse<StudentListItem>) => void
  setStudentDetail: (detail: StudentDetail) => void
  setStudentStats: (stats: StudentStats) => void
  setStudentsLoading: (loading: boolean) => void
  setStudentsError: (error: string | null) => void
  // Vocabulary methods
  setVocabularyList: (list: PageResponse<VocabularyListItem>) => void
  setVocabularyDetail: (detail: VocabularyDetail) => void
  setCefrStats: (stats: CefrStats) => void
  setCategories: (categories: PageResponse<Category>) => void
  setVocabularyLoading: (loading: boolean) => void
  setVocabularyError: (error: string | null) => void
}

const admin: AdminUser = {
  id: 1,
  name: "Nguyen Minh Anh",
  email: "admin@vocabularyai.vn",
  role: "ADMIN",
  avatar: "NA",
}

export const useAppStore = create<AppState>((set, get) => ({
  theme: localStorage.getItem("vocabularyai-theme") as Theme || "light",
  sidebarCollapsed: false,
  mobileOpen: false,
  isAuthenticated: sessionStorage.getItem("vocabularyai-auth") !== "signed-out",
  user:
    sessionStorage.getItem("vocabularyai-auth") === "signed-out" ? null : admin,
  toasts: [],
  // Dashboard data initial state
  dashboardSummary: null,
  dashboardRevenue: null,
  topStudents: null,
  activityTrend: null,
  dashboardLoading: false,
  dashboardError: null,
  // Students data initial state
  studentList: null,
  studentDetail: null,
  studentStats: null,
  studentsLoading: false,
  studentsError: null,
  // Vocabulary data initial state
  vocabularyList: null,
  vocabularyDetail: null,
  cefrStats: null,
  categories: null,
  vocabularyLoading: false,
  vocabularyError: null,
  setTheme: (theme) => {
    localStorage.setItem("vocabularyai-theme", theme)
    set({ theme })
  },
  toggleSidebar: () =>
    set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
  setMobileOpen: (mobileOpen) => set({ mobileOpen }),
  login: async (email, password) => {
    await new Promise((resolve) => window.setTimeout(resolve, 650))
    if (email === "admin@vocabularyai.vn" && password.length >= 6) {
      sessionStorage.setItem("vocabularyai-auth", "signed-in")
      set({ isAuthenticated: true, user: admin })
      return true
    }
    return false
  },
  logout: () => {
    sessionStorage.setItem("vocabularyai-auth", "signed-out")
    set({ isAuthenticated: false, user: null })
  },
  notify: (message, tone = "success") => {
    const id = Date.now()
    set((state) => ({ toasts: [...state.toasts, { id, message, tone }] }))
    window.setTimeout(() => get().dismissToast(id), 3500)
  },
  dismissToast: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    })),
  // Dashboard setters
  setDashboardSummary: (summary) => set({ dashboardSummary: summary }),
  setDashboardRevenue: (revenue) => set({ dashboardRevenue: revenue }),
  setTopStudents: (students) => set({ topStudents: students }),
  setActivityTrend: (trend) => set({ activityTrend: trend }),
  setDashboardLoading: (loading) => set({ dashboardLoading: loading }),
  setDashboardError: (error) => set({ dashboardError: error }),
  // Students setters
  setStudentList: (list) => set({ studentList: list }),
  setStudentDetail: (detail) => set({ studentDetail: detail }),
  setStudentStats: (stats) => set({ studentStats: stats }),
  setStudentsLoading: (loading) => set({ studentsLoading: loading }),
  setStudentsError: (error) => set({ studentsError: error }),
  // Vocabulary setters
  setVocabularyList: (list) => set({ vocabularyList: list }),
  setVocabularyDetail: (detail) => set({ vocabularyDetail: detail }),
  setCefrStats: (stats) => set({ cefrStats: stats }),
  setCategories: (categories) => set({ categories }),
  setVocabularyLoading: (loading) => set({ vocabularyLoading: loading }),
  setVocabularyError: (error) => set({ vocabularyError: error }),
}))
