import { AlertCircle, CheckCircle, AlertTriangle, XCircle } from "lucide-react"
import { Card } from "../common/ui"

interface HealthStatusCardProps {
  status: string
  totalUsers: number
  apiCallsToday: number
  averageResponseTime: number
  errorRate: number
}

export function HealthStatusCard({
  status,
  totalUsers,
  apiCallsToday,
  averageResponseTime,
  errorRate,
}: HealthStatusCardProps) {
  const getStatusIcon = () => {
    switch (status?.toUpperCase()) {
      case "HEALTHY":
        return <CheckCircle className="size-8 text-green-500" />
      case "WARNING":
        return <AlertTriangle className="size-8 text-yellow-500" />
      case "DEGRADED":
        return <AlertCircle className="size-8 text-orange-500" />
      case "CRITICAL":
        return <XCircle className="size-8 text-red-500" />
      default:
        return <CheckCircle className="size-8 text-gray-400" />
    }
  }

  const getStatusLabel = () => {
    switch (status?.toUpperCase()) {
      case "HEALTHY":
        return "All systems operational"
      case "WARNING":
        return "Issues detected"
      case "DEGRADED":
        return "Performance degraded"
      case "CRITICAL":
        return "Critical issues"
      default:
        return "Unknown status"
    }
  }

  return (
    <Card className="p-6">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-muted-foreground">
            System Health
          </p>
          <p className="mt-3 text-lg font-bold flex items-center gap-2">
            {getStatusIcon()}
            {getStatusLabel()}
          </p>
          <div className="mt-6 space-y-3 text-xs">
            <div className="flex justify-between">
              <span className="text-muted-foreground">API Calls Today</span>
              <span className="font-mono font-bold">{apiCallsToday.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Avg Response Time</span>
              <span className="font-mono font-bold">{averageResponseTime}ms</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Error Rate</span>
              <span
                className={`font-mono font-bold ${
                  errorRate > 5 ? "text-red-600" : errorRate > 2 ? "text-yellow-600" : "text-green-600"
                }`}
              >
                {errorRate.toFixed(2)}%
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Total Users</span>
              <span className="font-mono font-bold">{totalUsers.toLocaleString()}</span>
            </div>
          </div>
        </div>
      </div>
    </Card>
  )
}
