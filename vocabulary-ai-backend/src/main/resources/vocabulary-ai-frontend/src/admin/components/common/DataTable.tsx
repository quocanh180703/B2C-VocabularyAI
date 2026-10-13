import { ReactNode } from "react"

export interface DataTableColumn<T> {
  key: keyof T
  label: string
  width?: string
  render?: (value: any, row: T) => ReactNode
}

interface DataTableProps<T> {
  data: T[]
  columns: DataTableColumn<T>[]
  loading?: boolean
  keyField?: keyof T
}

export function DataTable<T extends Record<string, any>>({
  data,
  columns,
  loading,
  keyField = "id" as keyof T,
}: DataTableProps<T>) {
  if (loading) {
    return (
      <div className="w-full h-64 bg-muted/50 animate-pulse rounded" />
    )
  }

  if (data.length === 0) {
    return (
      <div className="w-full text-center py-8 text-muted-foreground">
        No data available
      </div>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-full text-left text-sm">
        <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
          <tr>
            {columns.map((col) => (
              <th key={String(col.key)} className="px-4 py-3" style={{ width: col.width }}>
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {data.map((row) => (
            <tr key={String(row[keyField])} className="hover:bg-muted/40">
              {columns.map((col) => (
                <td key={String(col.key)} className="px-4 py-4">
                  {col.render
                    ? col.render(row[col.key], row)
                    : row[col.key] ?? "—"}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
