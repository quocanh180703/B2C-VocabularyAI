import { useEffect, useState } from "react"
import {
  BookOpenCheck,
  BrainCircuit,
  CircleDollarSign,
  ClipboardCheck,
  RefreshCw,
  UserCheck,
  Users,
} from "lucide-react"
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts"
import ChartCard from "../components/common/ChartCard"
import PageHeader from "../components/common/PageHeader"
import StatCard from "../components/common/StatCard"
import { Badge, Button, Card, Select } from "../components/common/ui"
import { cefrDistribution } from "../data/platform"
import { useAppStore } from "../store/useAppStore"
import { dashboardService } from "../services/dashboardService"
import type {
  DashboardSummary,
  DashboardRevenue,
  ActivityTrend,
  TopStudent,
} from "../services/dashboardService"

const colors = [
  "var(--primary)",
  "var(--info)",
  "var(--success)",
  "var(--warning)",
  "var(--danger)",
]

/**
 * Format Vietnamese currency
 */
const formatVND = (value: number): string => {
  if (value >= 1_000_000) return `₫${(value / 1_000_000).toFixed(1)}M`
  if (value >= 1_000) return `₫${(value / 1_000).toFixed(1)}K`
  return `₫${value}`
}

export default function DashboardPage() {
  const [range, setRange] = useState("30 Days")
  const [isRefreshing, setIsRefreshing] = useState(false)
  const notify = useAppStore((state) => state.notify)

  // Dashboard data from store
  const summary = useAppStore((state) => state.dashboardSummary)
  const revenue = useAppStore((state) => state.dashboardRevenue)
  const topStudents = useAppStore((state) => state.topStudents)
  const activityTrend = useAppStore((state) => state.activityTrend)
  const loading = useAppStore((state) => state.dashboardLoading)
  const error = useAppStore((state) => state.dashboardError)

  // Store setters
  const setSummary = useAppStore((state) => state.setDashboardSummary)
  const setRevenue = useAppStore((state) => state.setDashboardRevenue)
  const setTopStudents = useAppStore((state) => state.setTopStudents)
  const setActivityTrend = useAppStore((state) => state.setActivityTrend)
  const setLoading = useAppStore((state) => state.setDashboardLoading)
  const setError = useAppStore((state) => state.setDashboardError)

  /**
   * Fetch all dashboard data
   */
  const loadDashboard = async () => {
    try {
      setLoading(true)
      setError(null)

      // Fetch all metrics in parallel
      const [summaryData, revenueData, studentsData, trendData] =
        await Promise.all([
          dashboardService.getSummary(),
          dashboardService.getRevenue(),
          dashboardService.getTopStudents(),
          dashboardService.getActivityTrend(),
        ])

      setSummary(summaryData)
      setRevenue(revenueData)
      setTopStudents(studentsData)
      setActivityTrend(trendData)

      notify("Dashboard data loaded successfully", "success")
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Failed to load dashboard"
      setError(errorMessage)
      notify(errorMessage, "error")
    } finally {
      setLoading(false)
    }
  }

  /**
   * Initial load on component mount
   */
  useEffect(() => {
    loadDashboard()
  }, [])

  /**
   * Handle refresh button
   */
  const handleRefresh = async () => {
    setIsRefreshing(true)
    await loadDashboard()
    setIsRefreshing(false)
  }

  // Show loading state
  if (loading && !summary) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">
          <div className="mb-4 text-lg font-semibold">Loading dashboard...</div>
          <div className="text-sm text-muted-foreground">
            Fetching real-time data from server
          </div>
        </div>
      </div>
    )
  }

  // Show error state
  if (error && !summary) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">
          <div className="mb-4 text-lg font-semibold text-danger">Error</div>
          <div className="mb-6 text-sm text-muted-foreground">{error}</div>
          <Button onClick={handleRefresh}>Retry</Button>
        </div>
      </div>
    )
  }

  return (
    <>
      <PageHeader
        title="Good morning, Admin"
        description="Monitor and manage your VocabularyAI platform."
        actions={
          <>
            <Select
              value={range}
              onChange={(event) => setRange(event.target.value)}
            >
              <option>7 Days</option>
              <option>30 Days</option>
              <option>3 Months</option>
              <option>6 Months</option>
              <option>12 Months</option>
            </Select>
            <Button
              variant="secondary"
              onClick={handleRefresh}
              disabled={isRefreshing}
            >
              <RefreshCw
                className={`size-4 ${isRefreshing ? "animate-spin" : ""}`}
              />
              {isRefreshing ? "Refreshing..." : "Refresh"}
            </Button>
          </>
        }
      />

      {/* Summary Stats */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <StatCard
          label="Total Students"
          value={summary?.totalUsers?.toLocaleString() || "0"}
          icon={Users}
        />
        <StatCard
          label="Active Subscriptions"
          value={summary?.activeSubscriptions?.toLocaleString() || "0"}
          icon={UserCheck}
          tone="success"
        />
        <StatCard
          label="Today's Learning"
          value={summary?.activeLearningSessionsToday?.toLocaleString() || "0"}
          icon={BookOpenCheck}
          tone="info"
        />
        <StatCard
          label="Total Revenue"
          value={formatVND(summary?.totalRevenue || 0)}
          icon={CircleDollarSign}
          tone="success"
        />
        <StatCard
          label="Today's Revenue"
          value={formatVND(revenue?.revenueToday || 0)}
          icon={CircleDollarSign}
        />
        <StatCard
          label="Avg Payment"
          value={formatVND(revenue?.averagePaymentAmount || 0)}
          icon={CircleDollarSign}
          tone="warning"
        />
      </div>

      {/* Charts */}
      <div className="mt-6 grid gap-6 xl:grid-cols-2">
        {/* Revenue Chart */}
        {activityTrend && (
          <ChartCard
            title="Daily Revenue"
            description={`Revenue trend · ${range}`}
            action={
              <Badge tone="success">
                +
                {(
                  (activityTrend.dailyRevenue.reduce((a, b) => a + b, 0) /
                    activityTrend.dailyRevenue.length) *
                  100
                ).toFixed(1)}
                %
              </Badge>
            }
          >
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart
                  data={activityTrend.dates.map((date, i) => ({
                    name: date,
                    revenue: activityTrend.dailyRevenue[i],
                  }))}
                  margin={{ left: -20 }}
                >
                  <defs>
                    <linearGradient id="revenue" x1="0" y1="0" x2="0" y2="1">
                      <stop
                        offset="5%"
                        stopColor="var(--primary)"
                        stopOpacity={0.28}
                      />
                      <stop
                        offset="95%"
                        stopColor="var(--primary)"
                        stopOpacity={0}
                      />
                    </linearGradient>
                  </defs>
                  <CartesianGrid vertical={false} stroke="var(--border)" />
                  <XAxis
                    dataKey="name"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                  />
                  <YAxis
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                  />
                  <Tooltip
                    contentStyle={{
                      background: "var(--card)",
                      borderColor: "var(--border)",
                      borderRadius: 12,
                    }}
                    formatter={(value) => formatVND(value as number)}
                  />
                  <Area
                    type="monotone"
                    dataKey="revenue"
                    name="Revenue"
                    stroke="var(--primary)"
                    strokeWidth={3}
                    fill="url(#revenue)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </ChartCard>
        )}

        {/* Active Users Chart */}
        {activityTrend && (
          <ChartCard
            title="Active Users"
            description={`Daily active users · ${range}`}
          >
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart
                  data={activityTrend.dates.map((date, i) => ({
                    name: date,
                    users: activityTrend.activeUsers[i],
                  }))}
                  margin={{ left: -20 }}
                >
                  <CartesianGrid vertical={false} stroke="var(--border)" />
                  <XAxis
                    dataKey="name"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                  />
                  <YAxis
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
                  />
                  <Tooltip
                    contentStyle={{
                      background: "var(--card)",
                      borderColor: "var(--border)",
                      borderRadius: 12,
                    }}
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="users"
                    name="Active Users"
                    stroke="var(--primary)"
                    strokeWidth={3}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </ChartCard>
        )}

        {/* Learning Sessions Chart */}
        {activityTrend && (
          <ChartCard title="Learning Sessions" description="Daily activities">
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={activityTrend.dates.map((date, i) => ({
                    name: date,
                    sessions: activityTrend.learningSessions[i],
                  }))}
                >
                  <CartesianGrid vertical={false} stroke="var(--border)" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} />
                  <YAxis axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{
                      background: "var(--card)",
                      borderColor: "var(--border)",
                      borderRadius: 12,
                    }}
                  />
                  <Bar
                    dataKey="sessions"
                    name="Sessions"
                    fill="var(--primary)"
                    radius={[8, 8, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </ChartCard>
        )}

        {/* Top Students */}
        {topStudents && topStudents.length > 0 && (
          <ChartCard title="Top Students" description="Best performers">
            <div className="space-y-4 pt-2">
              {topStudents.slice(0, 5).map((student) => (
                <div key={student.userId} className="flex items-center gap-3">
                  <div className="flex size-8 items-center justify-center rounded-full bg-primary-soft text-xs font-bold text-primary">
                    {student.rank}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="truncate text-sm font-semibold">
                      {student.fullName}
                    </div>
                    <div className="truncate text-xs text-muted-foreground">
                      {student.totalSessions} sessions • {student.accuracyRate.toFixed(1)}%
                    </div>
                  </div>
                  <Badge tone="success">
                    {student.wordsLearned} words
                  </Badge>
                </div>
              ))}
            </div>
          </ChartCard>
        )}
      </div>
    </>
  )
}
