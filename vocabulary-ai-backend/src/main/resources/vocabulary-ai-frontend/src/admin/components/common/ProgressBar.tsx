interface ProgressBarProps {
  value: number
  max?: number
  label?: string
  showLabel?: boolean
  color?: "blue" | "green" | "yellow" | "red"
  size?: "small" | "medium" | "large"
}

const colorClasses = {
  blue: "bg-blue-500",
  green: "bg-green-500",
  yellow: "bg-yellow-500",
  red: "bg-red-500",
}

const sizeClasses = {
  small: "h-1",
  medium: "h-2",
  large: "h-3",
}

export function ProgressBar({
  value,
  max = 100,
  label,
  showLabel = true,
  color = "blue",
  size = "medium",
}: ProgressBarProps) {
  const percentage = Math.min((value / max) * 100, 100)

  return (
    <div className="space-y-1">
      {(label || showLabel) && (
        <div className="flex justify-between text-xs">
          <span className="font-medium text-muted-foreground">{label}</span>
          <span className="font-mono text-muted-foreground">
            {value.toLocaleString()}{max !== 100 ? ` / ${max.toLocaleString()}` : "%"}
          </span>
        </div>
      )}
      <div className={`w-full bg-muted rounded-full overflow-hidden ${sizeClasses[size]}`}>
        <div
          className={`${colorClasses[color]} rounded-full transition-all duration-300`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  )
}
