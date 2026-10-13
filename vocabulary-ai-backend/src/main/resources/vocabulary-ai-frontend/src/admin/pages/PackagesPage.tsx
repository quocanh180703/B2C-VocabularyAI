import { Eye, Plus, Edit2, Trash2 } from "lucide-react"
import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router"
import PageHeader from "../components/common/PageHeader"
import {
  Badge,
  Button,
  Card,
  EmptyState,
  LoadingState,
  Pagination,
  SearchInput,
  Select,
  Modal,
} from "../components/common/ui"
import { packagesService, type PackageListItem, type PackageDetail } from "../services/packagesService"
import PackageFormModal, { type PackageFormData } from "../components/forms/PackageFormModal"
import { packageCrudService } from "../services/package-crud-service"
import { useAppStore } from "../store/useAppStore"

// Helper function to get status color
const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case "ACTIVE":
      return "success"
    case "INACTIVE":
      return "warning"
    case "ARCHIVED":
      return "error"
    default:
      return "info"
  }
}

export function PackageDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<PackageDetail | null>()
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    if (!id) return
    packagesService
      .getDetail(id)
      .then(setRecord)
      .catch(() => setRecord(null))
      .finally(() => setLoading(false))
  }, [id])
  
  if (loading)
    return (
      <Card>
        <LoadingState />
      </Card>
    )
  if (!record)
    return (
      <Card>
        <EmptyState
          title="Package not found"
          description="The requested package does not exist."
        />
      </Card>
    )
  return (
    <>
      <PageHeader
        title={record.name}
        description={`${record.code} • ${record.status}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/packages")}
          >
            Back to packages
          </Button>
        }
      />
      <div className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
        <Card className="p-6">
          <div className="flex gap-2">
            <Badge tone={getStatusColor(record.status)}>
              {record.status}
            </Badge>
          </div>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Description
          </div>
          <p className="mt-2 text-lg">{record.description}</p>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Pricing
          </div>
          <p className="mt-2 text-3xl font-bold font-mono">
            ${record.price.toFixed(2)}
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            for {record.durationDays} days
          </p>
        </Card>
        <Card className="p-6">
          <div className="font-bold">
            Package Information
          </div>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <b>Package Code</b>
              <p className="mt-1 text-muted-foreground font-mono">
                {record.code}
              </p>
            </div>
            <div>
              <b>Duration</b>
              <p className="mt-1 text-muted-foreground">
                {record.durationDays} days
              </p>
            </div>
            <div>
              <b>Total Purchases</b>
              <p className="mt-1 text-muted-foreground font-mono">
                {record.totalPurchases}
              </p>
            </div>
            <div>
              <b>Total Revenue</b>
              <p className="mt-1 text-muted-foreground font-mono font-bold">
                ${record.revenue.toFixed(2)}
              </p>
            </div>
            <div>
              <b>Active Subscriptions</b>
              <p className="mt-1 text-muted-foreground font-mono">
                {record.activeSubscriptions}
              </p>
            </div>
            <div>
              <b>Created</b>
              <p className="mt-1 text-muted-foreground">
                {new Date(record.createdAt).toLocaleDateString()}
              </p>
            </div>
          </div>
        </Card>
      </div>
    </>
  )
}

export default function PackagesPage() {
  const [records, setRecords] = useState<PackageListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState("")
  const [status, setStatus] = useState("All")
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const { notify } = useAppStore()
  const navigate = useNavigate()
  const pageSize = 20
  
  // Form modal state
  const [formModalOpen, setFormModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<"create" | "edit">("create")
  const [editingRecord, setEditingRecord] = useState<PackageListItem | null>(null)
  
  // Delete confirmation state
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<PackageListItem | null>(null)

  useEffect(() => {
    loadPackages()
  }, [page, status, query])

  const loadPackages = async () => {
    setLoading(true)
    try {
      const result = await packagesService.list(
        page - 1, // Convert to 0-based
        pageSize,
        "createdAt,desc",
        status !== "All" ? status : undefined,
        query || undefined
      )
      setRecords(result.content)
      setTotalPages(result.totalPages)
    } catch (error) {
      notify("Failed to load packages", "error")
      setRecords([])
    } finally {
      setLoading(false)
    }
  }
  
  const handleCreate = () => {
    setFormMode("create")
    setEditingRecord(null)
    setFormModalOpen(true)
  }
  
  const handleEdit = (record: PackageListItem) => {
    setFormMode("edit")
    setEditingRecord(record)
    setFormModalOpen(true)
  }
  
  const handleFormSubmit = async (data: PackageFormData) => {
    if (formMode === "create") {
      await packageCrudService.create(data)
    } else if (editingRecord) {
      await packageCrudService.update(editingRecord.id as number, data)
    }
    // Reload package list
    loadPackages()
  }
  
  const handleDelete = (record: PackageListItem) => {
    setDeleteTarget(record)
    setDeleteConfirmOpen(true)
  }
  
  const confirmDelete = async () => {
    if (!deleteTarget) return
    try {
      await packageCrudService.delete(deleteTarget.id as number)
      notify("Package deleted successfully", "success")
      setDeleteConfirmOpen(false)
      setDeleteTarget(null)
      loadPackages()
    } catch (error) {
      notify(`Failed to delete package: ${error}`, "error")
    }
  }

  return (
    <>
      <PageHeader
        title="Subscription Packages"
        description="Manage subscription packages and pricing."
        actions={
          <Button onClick={handleCreate}>
            <Plus className="size-4 mr-2" />
            Add Package
          </Button>
        }
      />
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1fr_auto]">
          <SearchInput
            placeholder="Search package name or code..."
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setPage(1)
            }}
          />
          <Select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            <option>ACTIVE</option>
            <option>INACTIVE</option>
            <option>ARCHIVED</option>
          </Select>
        </div>
        {loading ? (
          <LoadingState />
        ) : records.length === 0 ? (
          <EmptyState
            title="No packages found"
            description="Change your filters or search terms."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1300px] text-left text-sm">
                <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                  <tr>
                    {[
                      "Name",
                      "Code",
                      "Price",
                      "Duration",
                      "Status",
                      "Purchases",
                      "Revenue",
                      "Created",
                      "Actions",
                    ].map((label) => (
                      <th key={label} className="px-4 py-3">
                        {label}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {records.map((item) => (
                    <tr key={item.id} className="hover:bg-muted/40">
                      <td className="px-4 py-4 font-bold">{item.name}</td>
                      <td className="px-4 py-4 text-muted-foreground font-mono">
                        {item.code}
                      </td>
                      <td className="px-4 py-4 font-mono font-bold">
                        ${item.price.toFixed(2)}
                      </td>
                      <td className="px-4 py-4">
                        {item.durationDays} days
                      </td>
                      <td className="px-4 py-4">
                        <Badge tone={getStatusColor(item.status)}>
                          {item.status}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-center">
                        {item.purchaseCount}
                      </td>
                      <td className="px-4 py-4 font-mono font-bold">
                        ${item.revenue.toFixed(2)}
                      </td>
                      <td className="px-4 py-4 text-muted-foreground text-xs">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-4 flex gap-2">
                        <Button
                          variant="ghost"
                          className="size-8 min-h-8 px-0"
                          title="View"
                          onClick={() =>
                            navigate(`/admin/packages/${item.id}`)
                          }
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          className="size-8 min-h-8 px-0"
                          title="Edit"
                          onClick={() => handleEdit(item)}
                        >
                          <Edit2 className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          className="size-8 min-h-8 px-0 text-red-600 hover:text-red-700"
                          title="Delete"
                          onClick={() => handleDelete(item)}
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination page={page} pages={totalPages} onChange={setPage} />
          </>
        )}
      </Card>
      
      {/* Form Modal */}
      <PackageFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false)
          setEditingRecord(null)
        }}
        mode={formMode}
        initialData={editingRecord ? {
          code: editingRecord.code,
          name: editingRecord.name,
          price: editingRecord.price,
          durationDays: editingRecord.durationDays,
          status: editingRecord.status,
          description: editingRecord.description,
        } : undefined}
        onSubmit={handleFormSubmit}
      />
      
      {/* Delete Confirmation Modal */}
      <Modal
        open={deleteConfirmOpen}
        onClose={() => setDeleteConfirmOpen(false)}
        title="Confirm Deletion"
        footer={
          <div className="flex gap-2 justify-end">
            <Button variant="secondary" onClick={() => setDeleteConfirmOpen(false)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={confirmDelete}>
              Delete
            </Button>
          </div>
        }
      >
        <p className="text-sm text-muted-foreground">
          Are you sure you want to delete <strong>{deleteTarget?.name}</strong>? This action cannot be undone.
        </p>
      </Modal>
    </>
  )
}
