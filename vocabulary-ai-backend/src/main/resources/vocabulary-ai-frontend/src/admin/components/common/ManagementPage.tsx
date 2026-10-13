import { Download, Plus, RefreshCw } from "lucide-react"
import { useMemo, useState } from "react"
import type { TableRecord } from "../../types"
import { useAppStore } from "../../store/useAppStore"
import PageHeader from "./PageHeader"
import DataTable, { type TableColumn } from "./DataTable"
import { Button, Card, Input, Modal, SearchInput, Select } from "./ui"

export default function ManagementPage({
  title,
  description,
  rows: initialRows,
  columns,
  stats,
  primaryAction = "Create record",
  allowCreate = false,
}: {
  title: string
  description: string
  rows: TableRecord[]
  columns: TableColumn[]
  stats?: { label: string; value: string }[]
  primaryAction?: string
  allowCreate?: boolean
}) {
  const [rows, setRows] = useState(initialRows)
  const [query, setQuery] = useState("")
  const [status, setStatus] = useState("All")
  const [selected, setSelected] = useState<TableRecord | null>(null)
  const [editing, setEditing] = useState<TableRecord | null>(null)
  const [deleting, setDeleting] = useState<TableRecord | null>(null)
  const [name, setName] = useState("")
  const { notify } = useAppStore()
  const filtered = useMemo(
    () =>
      rows.filter(
        (row) =>
          Object.values(row).some((value) =>
            String(value).toLowerCase().includes(query.toLowerCase()),
          ) &&
          (status === "All" ||
            Object.values(row).some((value) => String(value) === status)),
      ),
    [rows, query, status],
  )
  const save = () => {
    if (!name.trim()) {
      notify("Please enter a valid name", "error")
      return
    }
    if (editing)
      setRows(
        rows.map((row) =>
          row.id === editing.id
            ? { ...row, [columns[0]?.key ?? "name"]: name }
            : row,
        ),
      )
    else
      setRows([
        { id: Date.now(), [columns[0]?.key ?? "name"]: name, status: "Active" },
        ...rows,
      ])
    notify(
      editing
        ? `${title} record updated successfully.`
        : `${title} record created successfully.`,
    )
    setEditing(null)
    setName("")
  }
  const exportCsv = () => {
    const csv = [
      columns.map((c) => c.label).join(","),
      ...filtered.map((row) =>
        columns.map((c) => String(row[c.key] ?? "")).join(","),
      ),
    ].join("\n")
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv" }))
    const link = document.createElement("a")
    link.href = url
    link.download = `${title.toLowerCase().split(" ").join("-")}.csv`
    link.click()
    URL.revokeObjectURL(url)
    notify("CSV exported successfully.")
  }
  return (
    <>
      <PageHeader
        title={title}
        description={description}
        actions={
          <>
            <Button
              variant="secondary"
              onClick={() => {
                notify("Data refreshed.", "info")
              }}
            >
              <RefreshCw className="size-4" />
              Refresh
            </Button>
            <Button variant="secondary" onClick={exportCsv}>
              <Download className="size-4" />
              Export
            </Button>
            {allowCreate && (
              <Button
                onClick={() => {
                  setEditing({ id: 0 })
                  setName("")
                }}
              >
                <Plus className="size-4" />
                {primaryAction}
              </Button>
            )}
          </>
        }
      />
      {stats && (
        <div className="mb-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {stats.map((stat) => (
            <Card key={stat.label} className="p-5">
              <div className="text-sm text-muted-foreground">{stat.label}</div>
              <div className="mt-2 text-2xl font-extrabold">{stat.value}</div>
            </Card>
          ))}
        </div>
      )}
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 sm:grid-cols-[1fr_auto]">
          <SearchInput
            placeholder={`Search ${title.toLowerCase()}...`}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <Select
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option>All</option>
            <option>Active</option>
            <option>Inactive</option>
            <option>Passed</option>
            <option>Failed</option>
            <option>PENDING</option>
            <option>PAID</option>
          </Select>
        </div>
        <DataTable
          rows={filtered}
          columns={columns}
          onView={setSelected}
          onEdit={
            allowCreate
              ? (row) => {
                  setEditing(row)
                  setName(String(row[columns[0]?.key ?? "name"] ?? ""))
                }
              : undefined
          }
          onDelete={allowCreate ? setDeleting : undefined}
        />
      </Card>
      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title={`${title} details`}
        wide
      >
        <div className="grid gap-4 sm:grid-cols-2">
          {selected &&
            Object.entries(selected).map(([key, value]) => (
              <div key={key} className="rounded-xl bg-muted p-4">
                <div className="text-[11px] font-bold uppercase tracking-wide text-muted-foreground">
                  {key.split("_").join(" ")}
                </div>
                <div className="mt-1 text-sm font-semibold">
                  {String(value)}
                </div>
              </div>
            ))}
        </div>
      </Modal>
      <Modal
        open={Boolean(editing)}
        onClose={() => setEditing(null)}
        title={editing?.id === 0 ? primaryAction : `Edit ${title}`}
        footer={
          <>
            <Button variant="secondary" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button onClick={save}>Save changes</Button>
          </>
        }
      >
        <label className="text-sm font-semibold">
          {columns[0]?.label}
          <Input
            className="mt-2"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </label>
      </Modal>
      <Modal
        open={Boolean(deleting)}
        onClose={() => setDeleting(null)}
        title="Confirm deletion"
        footer={
          <>
            <Button variant="secondary" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              onClick={() => {
                if (deleting)
                  setRows(rows.filter((row) => row.id !== deleting.id))
                setDeleting(null)
                notify("Record deleted successfully.", "warning")
              }}
            >
              Delete
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted-foreground">
          This action cannot be undone. The selected record will be permanently
          removed.
        </p>
      </Modal>
    </>
  )
}
