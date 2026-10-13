import { Eye, MessageSquare } from "lucide-react"
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
} from "../components/common/ui"
import { aiService, type AiConversationListItem, type AiConversationDetail } from "../services/aiService"
import { useAppStore } from "../store/useAppStore"

// Helper function to get type color
const getTypeColor = (type: string) => {
  const t = type?.toUpperCase()
  if (t === "GRAMMAR") return "info"
  if (t === "VOCABULARY") return "success"
  if (t === "HINT") return "warning"
  if (t === "GENERAL") return "default"
  return "default"
}

export function AiConversationDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<AiConversationDetail | null>()
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    if (!id) return
    aiService
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
          title="Conversation not found"
          description="The requested conversation does not exist."
        />
      </Card>
    )
  return (
    <>
      <PageHeader
        title={record.title}
        description={`${record.userEmail} • ${record.conversationType}${record.isArchived ? " • ARCHIVED" : ""}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/ai")}
          >
            Back to conversations
          </Button>
        }
      />
      <div className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
        <Card className="p-6">
          <div className="flex gap-2">
            <Badge tone={getTypeColor(record.conversationType)}>
              {record.conversationType}
            </Badge>
            {record.isArchived && <Badge tone="error">ARCHIVED</Badge>}
          </div>
          <div className="mt-5 text-sm font-bold text-muted-foreground">
            Summary
          </div>
          <p className="mt-2 text-lg">{record.summary || "—"}</p>
          <div className="mt-5 flex gap-2 text-sm">
            <MessageSquare className="size-4" />
            <span className="font-mono">{record.messageCount} messages</span>
          </div>
        </Card>
        <Card className="p-6">
          <div className="font-bold">
            Conversation Information
          </div>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <b>User Email</b>
              <p className="mt-1 text-muted-foreground">
                {record.userEmail}
              </p>
            </div>
            <div>
              <b>Type</b>
              <p className="mt-1 text-muted-foreground">
                {record.conversationType}
              </p>
            </div>
            <div>
              <b>Message Count</b>
              <p className="mt-1 text-muted-foreground font-mono">
                {record.messageCount}
              </p>
            </div>
            <div>
              <b>Status</b>
              <p className="mt-1 text-muted-foreground">
                {record.isArchived ? "Archived" : "Active"}
              </p>
            </div>
            <div>
              <b>Created</b>
              <p className="mt-1 text-muted-foreground">
                {new Date(record.createdAt).toLocaleString()}
              </p>
            </div>
          </div>
        </Card>
      </div>
    </>
  )
}

export default function AiConversationsPage() {
  const [records, setRecords] = useState<AiConversationListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState("")
  const [type, setType] = useState("All")
  const [userId, setUserId] = useState("")
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const { notify } = useAppStore()
  const navigate = useNavigate()
  const pageSize = 20

  useEffect(() => {
    loadConversations()
  }, [page, type, userId, query])

  const loadConversations = async () => {
    setLoading(true)
    try {
      const result = await aiService.list(
        page - 1, // Convert to 0-based
        pageSize,
        "createdAt,desc",
        userId || undefined,
        type !== "All" ? type : undefined,
        query || undefined
      )
      setRecords(result.content)
      setTotalPages(result.totalPages)
    } catch (error) {
      notify("Failed to load conversations", "error")
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <PageHeader
        title="AI Conversations"
        description="Monitor and analyze AI-assisted conversations with users."
      />
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1.5fr_auto_auto]">
          <SearchInput
            placeholder="Search by title or user email..."
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setPage(1)
            }}
          />
          <Select
            value={type}
            onChange={(event) => {
              setType(event.target.value)
              setPage(1)
            }}
          >
            <option>All</option>
            <option>GRAMMAR</option>
            <option>VOCABULARY</option>
            <option>HINT</option>
            <option>GENERAL</option>
          </Select>
        </div>
        {loading ? (
          <LoadingState />
        ) : records.length === 0 ? (
          <EmptyState
            title="No conversations found"
            description="Change your filters or search terms."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1100px] text-left text-sm">
                <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                  <tr>
                    {[
                      "Title",
                      "User",
                      "Type",
                      "Messages",
                      "Status",
                      "Updated",
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
                      <td className="px-4 py-4 font-bold truncate max-w-[200px]">
                        {item.title}
                      </td>
                      <td className="px-4 py-4 text-muted-foreground truncate">
                        {item.userEmail}
                      </td>
                      <td className="px-4 py-4">
                        <Badge tone={getTypeColor(item.conversationType)}>
                          {item.conversationType}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-center font-mono">
                        {item.messageCount}
                      </td>
                      <td className="px-4 py-4">
                        <Badge tone={item.isArchived ? "error" : "success"}>
                          {item.isArchived ? "Archived" : "Active"}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-muted-foreground text-xs">
                        {new Date(item.updatedAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-4 text-muted-foreground text-xs">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-4">
                        <Button
                          variant="ghost"
                          className="size-8 min-h-8 px-0"
                          onClick={() =>
                            navigate(`/admin/ai/${item.id}`)
                          }
                        >
                          <Eye className="size-4" />
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
    </>
  )
}
