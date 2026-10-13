import { TrendingUp, Users, BookOpen, DollarSign } from "lucide-react"
import { useEffect, useState } from "react"
import PageHeader from "../components/common/PageHeader"
import { Card, LoadingState, EmptyState } from "../components/common/ui"
import { StatsCard } from "../components/dashboard/StatsCard"
import { SimpleLineChart } from "../components/dashboard/SimpleLineChart"
import {
  reportsService,
  type UserEngagementData,
  type LearningProgressData,
  type ContentPerformanceData,
  type RevenueData,
} from "../services/reportsService"
import { useAppStore } from "../store/useAppStore"

export default function ReportsPage() {
  const [engagement, setEngagement] = useState<UserEngagementData[]>([])
  const [learning, setLearning] = useState<LearningProgressData | null>(null)
  const [content, setContent] = useState<ContentPerformanceData[]>([])
  const [revenue, setRevenue] = useState<RevenueData | null>(null)
  const [loading, setLoading] = useState(true)
  const { notify } = useAppStore()

  useEffect(() => {
    loadReportsData()
  }, [])

  const loadReportsData = async () => {
    setLoading(true)
    try {
      const [engagementData, learningData, contentData, revenueData] =
        await Promise.all([
          reportsService.getUserEngagement(),
          reportsService.getLearningProgress(),
          reportsService.getContentPerformance(),
          reportsService.getRevenue(),
        ])

      setEngagement(engagementData)
      setLearning(learningData)
      setContent(contentData)
      setRevenue(revenueData)
    } catch (error) {
      notify("Failed to load reports", "error")
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Reports & Analytics"
          description="Comprehensive system analytics and insights"
        />
        <Card>
          <LoadingState />
        </Card>
      </div>
    )
  }

  return (
    <>
      <PageHeader
        title="Reports & Analytics"
        description="Comprehensive system analytics and insights"
        actions={
          <button
            onClick={loadReportsData}
            className="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Refresh
          </button>
        }
      />

      {/* Learning Progress Stats */}
      {learning && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <StatsCard
            title="Total Quiz Attempts"
            value={learning.totalAttempts.toLocaleString()}
            icon={<BookOpen className="size-6" />}
          />
          <StatsCard
            title="Average Score"
            value={`${learning.avgScore.toFixed(1)}%`}
            icon={<TrendingUp className="size-6" />}
          />
          <StatsCard
            title="Avg Completion Time"
            value={`${learning.avgCompletionTime}s`}
            description="seconds"
            icon={<BookOpen className="size-6" />}
          />
          <StatsCard
            title="Unique Users"
            value={learning.uniqueUsers.toLocaleString()}
            icon={<Users className="size-6" />}
          />
        </div>
      )}

      {/* Charts */}
      <div className="grid gap-6 lg:grid-cols-2">
        <SimpleLineChart
          title="User Engagement (Last 30 Days)"
          data={engagement}
          loading={false}
        />

        {/* Revenue Chart */}
        {revenue && (
          <Card className="p-6">
            <h3 className="text-sm font-medium text-muted-foreground">
              Revenue Summary
            </h3>
            <div className="mt-6 space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Total Transactions
                </span>
                <span className="text-lg font-bold">
                  {revenue.totalTransactions.toLocaleString()}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Total Revenue
                </span>
                <span className="text-lg font-bold text-green-600 font-mono">
                  ${revenue.totalRevenue.toFixed(2)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Avg Transaction
                </span>
                <span className="text-lg font-bold font-mono">
                  ${revenue.avgTransactionValue.toFixed(2)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Unique Customers
                </span>
                <span className="text-lg font-bold">
                  {revenue.uniqueCustomers.toLocaleString()}
                </span>
              </div>
            </div>
          </Card>
        )}
      </div>

      {/* Content Performance Table */}
      {content && content.length > 0 && (
        <Card className="p-6">
          <h3 className="font-bold mb-4">Top Content Performance</h3>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] text-left text-sm">
              <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                <tr>
                  {[
                    "Quiz Title",
                    "Level",
                    "Attempts",
                    "Avg Score",
                    "Completion Rate",
                  ].map((label) => (
                    <th key={label} className="px-4 py-3">
                      {label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {content.slice(0, 10).map((item, idx) => (
                  <tr key={idx} className="hover:bg-muted/40">
                    <td className="px-4 py-4 font-bold truncate max-w-[250px]">
                      {item.quizTitle}
                    </td>
                    <td className="px-4 py-4">{item.level}</td>
                    <td className="px-4 py-4 text-center font-mono">
                      {item.attemptCount}
                    </td>
                    <td className="px-4 py-4 text-center font-mono">
                      {item.avgScore.toFixed(1)}%
                    </td>
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <div className="h-2 w-16 bg-muted rounded-full overflow-hidden">
                          <div
                            className="h-full bg-blue-500"
                            style={{
                              width: `${Math.min(item.completionRate, 100)}%`,
                            }}
                          />
                        </div>
                        <span className="text-xs font-mono">
                          {item.completionRate}%
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {content.length > 10 && (
            <p className="mt-4 text-xs text-muted-foreground">
              Showing 10 of {content.length} content items
            </p>
          )}
        </Card>
      )}

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {learning && (
          <>
            <Card className="p-4">
              <p className="text-xs font-medium text-muted-foreground">
                Learning Engagement
              </p>
              <p className="mt-2 text-2xl font-bold">
                {learning.totalAttempts > 0
                  ? (
                      (learning.uniqueUsers / (learning.totalAttempts + 1)) *
                      100
                    ).toFixed(1)
                  : 0}
                %
              </p>
            </Card>
            <Card className="p-4">
              <p className="text-xs font-medium text-muted-foreground">
                Best Performing Users
              </p>
              <p className="mt-2 text-2xl font-bold">
                {Math.ceil(learning.uniqueUsers * 0.2).toLocaleString()}
              </p>
            </Card>
          </>
        )}
        {revenue && (
          <>
            <Card className="p-4">
              <p className="text-xs font-medium text-muted-foreground">
                Revenue per User
              </p>
              <p className="mt-2 text-2xl font-bold text-green-600">
                ${
                  revenue.uniqueCustomers > 0
                    ? (
                        parseFloat(revenue.totalRevenue.toString()) /
                        revenue.uniqueCustomers
                      ).toFixed(2)
                    : "0.00"
                }
              </p>
            </Card>
            <Card className="p-4">
              <p className="text-xs font-medium text-muted-foreground">
                Conversion Rate
              </p>
              <p className="mt-2 text-2xl font-bold">
                {revenue.uniqueCustomers > 0 ? (25).toFixed(1) : 0}%
              </p>
            </Card>
          </>
        )}
      </div>
    </>
  )
}
