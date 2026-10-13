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
import { vocabularyService, type VocabularyListItem, type VocabularyDetail } from "../services/vocabularyService"
import VocabularyFormModal, { type VocabularyFormData } from "../components/forms/VocabularyFormModal"
import { vocabularyCrudService } from "../services/vocabulary-crud-service"
import { useAppStore } from "../store/useAppStore"

export function VocabularyDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<VocabularyDetail | null>()
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    if (!id) return
    vocabularyService
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
          title="Vocabulary not found"
          description="The requested word does not exist."
        />
      </Card>
    )
  return (
    <>
      <PageHeader
        title={record.word}
        description={`${record.pronunciation} · ${record.partOfSpeech} · CEFR ${record.cefr}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/vocabulary")}
          >
            Back to vocabulary
          </Button>
        }
      />
      <div className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
        <Card className="p-6">
          <Badge tone="info">{record.cefr}</Badge>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Definition
          </div>
          <p className="mt-2 text-lg">{record.definition}</p>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Pronunciation
          </div>
          <p className="mt-2 text-lg">{record.pronunciation}</p>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Part of Speech
          </div>
          <p className="mt-2 text-lg">{record.partOfSpeech}</p>
        </Card>
        <Card className="p-6">
          <div className="font-bold">
            Word Information
          </div>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <b>Source</b>
              <p className="mt-1 text-muted-foreground">
                {record.source}
              </p>
            </div>
            <div>
              <b>Language</b>
              <p className="mt-1 text-muted-foreground">
                {record.language}
              </p>
            </div>
            <div>
              <b>Usage Count</b>
              <p className="mt-1 text-muted-foreground">
                {record.usageCount}
              </p>
            </div>
            <div>
              <b>Created At</b>
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

export default function VocabularyPage() {
  const [records, setRecords] = useState<VocabularyListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState("")
  const [cefr, setCefr] = useState("All")
  const [pos, setPos] = useState("All")
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const { notify } = useAppStore()
  const navigate = useNavigate()
  const pageSize = 20
  
  // Form modal state
  const [formModalOpen, setFormModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<"create" | "edit">("create")
  const [editingRecord, setEditingRecord] = useState<VocabularyListItem | null>(null)
  
  // Delete confirmation state
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<VocabularyListItem | null>(null)

  useEffect(() => {
    loadVocabulary()
  }, [page, cefr, pos, query])

  const loadVocabulary = async () => {
    setLoading(true)
    try {
      const result = await vocabularyService.list(
        page - 1, // Convert to 0-based
        pageSize,
        "word,asc",
        cefr !== "All" ? cefr : undefined,
        pos !== "All" ? pos : undefined,
        query || undefined
      )
      setRecords(result.content)
      setTotalPages(result.totalPages)
    } catch (error) {
      notify("Failed to load vocabulary", "error")
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
  
  const handleEdit = (record: VocabularyListItem) => {
    setFormMode("edit")
    setEditingRecord(record)
    setFormModalOpen(true)
  }
  
  const handleFormSubmit = async (data: VocabularyFormData) => {
    if (formMode === "create") {
      await vocabularyCrudService.create(data)
    } else if (editingRecord) {
      await vocabularyCrudService.update(editingRecord.id, data)
    }
    // Reload vocabulary list
    loadVocabulary()
  }
  
  const handleDelete = (record: VocabularyListItem) => {
    setDeleteTarget(record)
    setDeleteConfirmOpen(true)
  }
  
  const confirmDelete = async () => {
    if (!deleteTarget) return
    try {
      await vocabularyCrudService.delete(deleteTarget.id)
      notify("Vocabulary deleted successfully", "success")
      setDeleteConfirmOpen(false)
      setDeleteTarget(null)
      loadVocabulary()
    } catch (error) {
      notify(`Failed to delete vocabulary: ${error}`, "error")
    }
  }

  return (
    <>
      <PageHeader
        title="Vocabulary Management"
        description="Browse the English vocabulary database (CEFR corpus)."
        actions={
          <Button onClick={handleCreate}>
            <Plus className="size-4 mr-2" />
            Add Vocabulary
          </Button>
        }
      />
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1fr_auto_auto]">
          <SearchInput
            placeholder="Search word or definition..."
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setPage(1)
            }}
          />
          <Select
            value={cefr}
            onChange={(event) => {
              setCefr(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            {["A1", "A2", "B1", "B2", "C1"].map((level) => (
              <option key={level}>{level}</option>
            ))}
          </Select>
          <Select
            value={pos}
            onChange={(event) => {
              setPos(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            <option>NOUN</option>
            <option>VERB</option>
            <option>ADJECTIVE</option>
            <option>ADVERB</option>
          </Select>
        </div>
        {loading ? (
          <LoadingState />
        ) : records.length === 0 ? (
          <EmptyState
            title="No vocabulary found"
            description="Change your filters or search terms."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1100px] text-left text-sm">
                <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                  <tr>
                    {[
                      "Word",
                      "Meaning",
                      "CEFR",
                      "Part of Speech",
                      "Pronunciation",
                      "Source",
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
                      <td className="px-4 py-4 font-bold">{item.word}</td>
                      <td className="max-w-48 truncate px-4 py-4 text-muted-foreground">
                        {item.meaning}
                      </td>
                      <td className="px-4 py-4">
                        <Badge tone="info">{item.cefr}</Badge>
                      </td>
                      <td className="px-4 py-4">{item.partOfSpeech}</td>
                      <td className="px-4 py-4 text-muted-foreground">
                        {item.pronunciation}
                      </td>
                      <td className="px-4 py-4 text-muted-foreground">
                        {item.source}
                      </td>
                      <td className="px-4 py-4 text-muted-foreground">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-4 flex gap-2">
                        <Button
                          variant="ghost"
                          className="size-8 min-h-8 px-0"
                          title="View"
                          onClick={() =>
                            navigate(`/admin/vocabulary/${item.id}`)
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
      <VocabularyFormModal
        open={formModalOpen}
        onClose={() => {
          setFormModalOpen(false)
          setEditingRecord(null)
        }}
        mode={formMode}
        initialData={editingRecord ? {
          word: editingRecord.word,
          meaning: editingRecord.meaning,
          partOfSpeech: editingRecord.partOfSpeech,
          level: editingRecord.cefr,
          pronunciation: editingRecord.pronunciation,
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
          Are you sure you want to delete <strong>{deleteTarget?.word}</strong>? This action cannot be undone.
        </p>
      </Modal>
    </>
  )
}
