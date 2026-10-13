import {
  Users,
  Activity,
  BarChart3,
  CheckCircle,
  MessageSquare,
} from "lucide-react"
import { useEffect, useState } from "react"
import PageHeader from "../components/common/PageHeader"
import { Card, LoadingState, EmptyState } from "../components/common/ui"
import { StatsCard } from "../components/dashboard/StatsCard"
import { HealthStatusCard } from "../components/dashboard/HealthStatusCard"
import { SimpleLineChart } from "../components/dashboard/SimpleLineChart"
import {
  systemService,
  type SystemOverview,
  type SystemHealth,
  type UserRegistrationTrend,
} from "../services/systemService"
import { useAppStore } from "../store/useAppStore"

export default function AdminSystemPage() {
  const [overview, setOverview] = useState<SystemOverview | null>(null)
  const [health, setHealth] = useState<SystemHealth | null>(null)
  const [trend, setTrend] = useState<UserRegistrationTrend[]>([])
  const [loading, setLoading] = useState(true)
  const { notify } = useAppStore()

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    setLoading(true)
    try {
      const [overviewData, healthData, trendData] = await Promise.all([
        systemService.getOverview(),
        systemService.getHealth(),
        systemService.getRegistrationTrend(),
      ])

      setOverview(overviewData)
      setHealth(healthData)
      setTrend(trendData)
    } catch (error) {
      notify("Failed to load dashboard data", "error")
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="System Dashboard"
          description="Real-time system metrics and analytics"
        />
        <Card>
          <LoadingState />
        </Card>
      </div>
    )
  }

  if (!overview || !health) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="System Dashboard"
          description="Real-time system metrics and analytics"
        />
        <Card>
          <EmptyState
            title="Failed to load dashboard"
            description="Please refresh the page and try again."
          />
        </Card>
      </div>
    )
  }

  return (
    <>
      <PageHeader
        title="System Dashboard"
        description="Real-time system metrics and analytics"
        actions={
          <button
            onClick={loadDashboardData}
            className="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Refresh
          </button>
        }
      />

      {/* Overview Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Users"
          value={overview.totalUsers.toLocaleString()}
          icon={<Users className="size-6" />}
          trend={{
            value: 12,
            label: "from last month",
            up: true,
          }}
        />
        <StatsCard
          title="Active Today"
          value={overview.activeUsersToday.toLocaleString()}
          icon={<Activity className="size-6" />}
          description="Users active in the last 24 hours"
        />
        <StatsCard
          title="Total Sessions"
          value={overview.totalSessions.toLocaleString()}
          icon={<CheckCircle className="size-6" />}
          trend={{
            value: 8,
            label: "vs last week",
            up: true,
          }}
        />
        <StatsCard
          title="Total Quizzes"
          value={overview.totalQuizzes.toLocaleString()}
          icon={<BarChart3 className="size-6" />}
          description="Active and archived"
        />
      </div>

      {/* System Health & Registration Trend */}
      <div className="grid gap-6 lg:grid-cols-[1.3fr_1.7fr]">
        <HealthStatusCard
          status={health.status}
          totalUsers={health.totalUsers}
          apiCallsToday={health.apiCallsToday}
          averageResponseTime={health.averageResponseTime}
          errorRate={health.errorRate}
        />
        <SimpleLineChart
          title="User Registration Trend (30 days)"
          data={trend}
          loading={false}
        />
      </div>

      {/* Additional Metrics */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card className="p-6">
          <h3 className="text-sm font-medium text-muted-foreground">
            User Engagement
          </h3>
          <p className="mt-3 text-3xl font-bold">
            {overview.avgUserEngagement.toFixed(1)}%
          </p>
          <p className="mt-2 text-xs text-muted-foreground">
            Average engagement rate across all users
          </p>
          <div className="mt-4 h-2 bg-muted rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-500 rounded-full"
              style={{
                width: `${Math.min(overview.avgUserEngagement, 100)}%`,
              }}
            />
          </div>
        </Card>

        <Card className="p-6">
          <h3 className="text-sm font-medium text-muted-foreground">
            System Status
          </h3>
          <div className="mt-3 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm">API Server</span>
              <span className="inline-flex items-center gap-2 text-xs font-medium">
                <span className="size-2 bg-green-500 rounded-full" />
                Online
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm">Database</span>
              <span className="inline-flex items-center gap-2 text-xs font-medium">
                <span className="size-2 bg-green-500 rounded-full" />
                Connected
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm">Cache</span>
              <span className="inline-flex items-center gap-2 text-xs font-medium">
                <span className="size-2 bg-green-500 rounded-full" />
                Active
              </span>
            </div>
          </div>
        </Card>
      </div>

      {/* Quick Stats */}
      <Card className="p-6">
        <h3 className="font-bold">Quick Statistics</h3>
        <div className="mt-4 grid gap-4 md:grid-cols-3">
          <div className="space-y-2">
            <p className="text-xs uppercase font-medium text-muted-foreground">
              Total Vocabularies
            </p>
            <p className="text-2xl font-bold">
              {overview.totalVocabularies.toLocaleString()}
            </p>
          </div>
          <div className="space-y-2">
            <p className="text-xs uppercase font-medium text-muted-foreground">
              Avg Response Time
            </p>
            <p className="text-2xl font-bold">{health.averageResponseTime}ms</p>
          </div>
          <div className="space-y-2">
            <p className="text-xs uppercase font-medium text-muted-foreground">
              Error Rate
            </p>
            <p
              className={`text-2xl font-bold ${
                health.errorRate > 5
                  ? "text-red-600"
                  : health.errorRate > 2
                    ? "text-yellow-600"
                    : "text-green-600"
              }`}
            >
              {health.errorRate.toFixed(2)}%
            </p>
          </div>
        </div>
      </Card>
    </>
  )
}
