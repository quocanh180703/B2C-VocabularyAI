import { Card } from "../common/ui"

interface ChartPoint {
  date: string
  count: number
}

interface SimpleLineChartProps {
  title: string
  data: ChartPoint[]
  loading?: boolean
}

export function SimpleLineChart({ title, data, loading }: SimpleLineChartProps) {
  if (loading) {
    return (
      <Card className="p-6">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <div className="mt-4 h-40 bg-muted/50 animate-pulse rounded" />
      </Card>
    )
  }

  if (!data || data.length === 0) {
    return (
      <Card className="p-6">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <div className="mt-8 text-center text-muted-foreground">
          No data available
        </div>
      </Card>
    )
  }

  // Calculate max value for scaling
  const maxValue = Math.max(...data.map((d) => d.count), 1)
  const minValue = Math.min(...data.map((d) => d.count))

  // Normalize data for display
  const range = maxValue - minValue || 1
  const normalized = data.map((d) => ({
    ...d,
    height: ((d.count - minValue) / range) * 100,
  }))

  return (
    <Card className="p-6">
      <p className="text-sm font-medium text-muted-foreground">{title}</p>
      <div className="mt-4">
        <div className="flex items-end justify-between gap-1">
          {normalized.map((point, idx) => (
            <div
              key={idx}
              className="flex-1 flex flex-col items-center gap-1"
              title={`${point.date}: ${point.count}`}
            >
              <div
                className="w-full bg-blue-500 rounded-t transition-all hover:bg-blue-600 hover:opacity-80"
                style={{
                  height: `${Math.max(point.height, 5)}px`,
                  minHeight: "4px",
                }}
              />
              {(idx % Math.ceil(data.length / 7) === 0 || idx === data.length - 1) && (
                <span className="text-xs text-muted-foreground truncate">
                  {new Date(point.date).toLocaleDateString("en-US", {
                    month: "short",
                    day: "numeric",
                  })}
                </span>
              )}
            </div>
          ))}
        </div>
        <div className="mt-4 flex justify-between text-xs text-muted-foreground">
          <span>Min: {minValue}</span>
          <span>Max: {maxValue}</span>
        </div>
      </div>
    </Card>
  )
}
