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
import { quizService, type QuizListItem, type QuizDetail } from "../services/quizService"
import QuizFormModal, { type QuizFormData } from "../components/forms/QuizFormModal"
import { quizCrudService } from "../services/quiz-crud-service"
import { useAppStore } from "../store/useAppStore"

// Helper function to get status color
const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case "PUBLISHED":
      return "success"
    case "DRAFT":
      return "warning"
    case "ARCHIVED":
      return "error"
    default:
      return "info"
  }
}

// Helper function to get level color
const getLevelColor = (level: string) => {
  const l = level?.toUpperCase()
  if (l?.startsWith("A")) return "info"
  if (l?.startsWith("B")) return "warning"
  if (l?.startsWith("C")) return "error"
  return "default"
}

export function QuizDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<QuizDetail | null>()
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    if (!id) return
    quizService
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
          title="Quiz not found"
          description="The requested quiz does not exist."
        />
      </Card>
    )
  return (
    <>
      <PageHeader
        title={record.title}
        description={`${record.category} • Level ${record.level} • ${record.status}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/quiz")}
          >
            Back to quizzes
          </Button>
        }
      />
      <div className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
        <Card className="p-6">
          <div className="flex gap-2">
            <Badge tone={getLevelColor(record.level)}>{record.level}</Badge>
            <Badge tone={getStatusColor(record.status)}>{record.status}</Badge>
          </div>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Description
          </div>
          <p className="mt-2 text-lg">{record.description}</p>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Category
          </div>
          <p className="mt-2 text-lg">{record.category}</p>
        </Card>
        <Card className="p-6">
          <div className="font-bold">
            Quiz Information
          </div>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <b>Time Limit (minutes)</b>
              <p className="mt-1 text-muted-foreground">
                {record.timeLimit}
              </p>
            </div>
            <div>
              <b>Total Attempts</b>
              <p className="mt-1 text-muted-foreground">
                {record.totalAttempts}
              </p>
            </div>
            <div>
              <b>Completed Attempts</b>
              <p className="mt-1 text-muted-foreground">
                {record.completedAttempts}
              </p>
            </div>
            <div>
              <b>Average Score</b>
              <p className="mt-1 text-muted-foreground">
                {record.avgScore.toFixed(1)}%
              </p>
            </div>
            <div>
              <b>Pass Rate</b>
              <p className="mt-1 text-muted-foreground">
                {record.passRate.toFixed(1)}%
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

export default function QuizPage() {
  const [records, setRecords] = useState<QuizListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState("")
  const [level, setLevel] = useState("All")
  const [status, setStatus] = useState("All")
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const { notify } = useAppStore()
  const navigate = useNavigate()
  const pageSize = 20
  
  // Form modal state
  const [formModalOpen, setFormModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<"create" | "edit">("create")
  const [editingRecord, setEditingRecord] = useState<QuizListItem | null>(null)
  
  // Delete confirmation state
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<QuizListItem | null>(null)

  useEffect(() => {
    loadQuizzes()
  }, [page, level, status, query])

  const loadQuizzes = async () => {
    setLoading(true)
    try {
      const result = await quizService.list(
        page - 1, // Convert to 0-based
        pageSize,
        "createdAt,desc",
        level !== "All" ? level : undefined,
        status !== "All" ? status : undefined,
        query || undefined
      )
      setRecords(result.content)
      setTotalPages(result.totalPages)
    } catch (error) {
      notify("Failed to load quizzes", "error")
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
  
  const handleEdit = (record: QuizListItem) => {
    setFormMode("edit")
    setEditingRecord(record)
    setFormModalOpen(true)
  }
  
  const handleFormSubmit = async (data: QuizFormData) => {
    if (formMode === "create") {
      await quizCrudService.create(data)
    } else if (editingRecord) {
      await quizCrudService.update(editingRecord.id as number, data)
    }
    // Reload quiz list
    loadQuizzes()
  }
  
  const handleDelete = (record: QuizListItem) => {
    setDeleteTarget(record)
    setDeleteConfirmOpen(true)
  }
  
  const confirmDelete = async () => {
    if (!deleteTarget) return
    try {
      await quizCrudService.delete(deleteTarget.id as number)
      notify("Quiz deleted successfully", "success")
      setDeleteConfirmOpen(false)
      setDeleteTarget(null)
      loadQuizzes()
    } catch (error) {
      notify(`Failed to delete quiz: ${error}`, "error")
    }
  }

  return (
    <>
      <PageHeader
        title="Quiz Management"
        description="Manage quiz content and track student performance."
        actions={
          <Button onClick={handleCreate}>
            <Plus className="size-4 mr-2" />
            Add Quiz
          </Button>
        }
      />
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1fr_auto_auto]">
          <SearchInput
            placeholder="Search quiz title or category..."
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setPage(1)
            }}
          />
          <Select
            value={level}
            onChange={(event) => {
              setLevel(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            {["A1", "A2", "B1", "B2", "C1", "C2"].map((l) => (
              <option key={l}>{l}</option>
            ))}
          </Select>
          <Select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            <option>PUBLISHED</option>
            <option>DRAFT</option>
            <option>ARCHIVED</option>
          </Select>
        </div>
        {loading ? (
          <LoadingState />
        ) : records.length === 0 ? (
          <EmptyState
            title="No quizzes found"
            description="Change your filters or search terms."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1200px] text-left text-sm">
                <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                  <tr>
                    {[
                      "Title",
                      "Level",
                      "Category",
                      "Time",
                      "Status",
                      "Attempts",
                      "Avg Score",
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
                      <td className="px-4 py-4 font-bold">{item.title}</td>
                      <td className="px-4 py-4">
                        <Badge tone={getLevelColor(item.level)}>{item.level}</Badge>
                      </td>
                      <td className="px-4 py-4 text-muted-foreground">
                        {item.category}
                      </td>
                      <td className="px-4 py-4 text-center font-mono">
                        {item.timeLimit}m
                      </td>
                      <td className="px-4 py-4">
                        <Badge tone={getStatusColor(item.status)}>
                          {item.status}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-center font-mono">
                        {item.attemptCount}
                      </td>
                      <td className="px-4 py-4 text-center">
                        {item.avgScore > 0 ? `${item.avgScore.toFixed(1)}%` : "—"}
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
                            navigate(`/admin/quiz/${item.id}`)
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
      <QuizFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false)
          setEditingRecord(null)
        }}
        mode={formMode}
        initialData={editingRecord ? {
          title: editingRecord.title,
          description: editingRecord.description,
          category: editingRecord.category,
          level: editingRecord.level,
          timeLimit: editingRecord.timeLimit,
          status: editingRecord.status,
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
          Are you sure you want to delete <strong>{deleteTarget?.title}</strong>? This action cannot be undone.
        </p>
      </Modal>
    </>
  )
}
