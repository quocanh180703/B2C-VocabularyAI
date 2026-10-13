import { Eye } from "lucide-react"
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
import { speakingService, type SpeakingSessionListItem, type SpeakingSessionDetail } from "../services/speakingService"
import { useAppStore } from "../store/useAppStore"

// Helper function to get status color
const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case "COMPLETED":
      return "success"
    case "IN_PROGRESS":
      return "warning"
    case "CANCELLED":
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

export function SpeakingDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<SpeakingSessionDetail | null>()
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    if (!id) return
    speakingService
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
          title="Speaking session not found"
          description="The requested session does not exist."
        />
      </Card>
    )
  return (
    <>
      <PageHeader
        title={record.topic}
        description={`${record.userEmail} • Level ${record.level} • ${record.status}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/speaking")}
          >
            Back to sessions
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
            Overall Score
          </div>
          <p className="mt-2 text-lg font-mono text-2xl">
            {record.overallScore.toFixed(1)}/100
          </p>
        </Card>
        <Card className="p-6">
          <div className="font-bold">
            Session Information
          </div>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <b>User Email</b>
              <p className="mt-1 text-muted-foreground">
                {record.userEmail}
              </p>
            </div>
            <div>
              <b>Total Attempts</b>
              <p className="mt-1 text-muted-foreground">
                {record.attemptCount}
              </p>
            </div>
            <div>
              <b>Started At</b>
              <p className="mt-1 text-muted-foreground">
                {new Date(record.startedAt).toLocaleString()}
              </p>
            </div>
            <div>
              <b>Completed At</b>
              <p className="mt-1 text-muted-foreground">
                {record.completedAt ? new Date(record.completedAt).toLocaleString() : "—"}
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

export default function SpeakingPage() {
  const [records, setRecords] = useState<SpeakingSessionListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [userId, setUserId] = useState("")
  const [level, setLevel] = useState("All")
  const [status, setStatus] = useState("All")
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const { notify } = useAppStore()
  const navigate = useNavigate()
  const pageSize = 20

  useEffect(() => {
    loadSessions()
  }, [page, level, status, userId])

  const loadSessions = async () => {
    setLoading(true)
    try {
      const result = await speakingService.list(
        page - 1, // Convert to 0-based
        pageSize,
        "createdAt,desc",
        userId || undefined,
        level !== "All" ? level : undefined,
        status !== "All" ? status : undefined
      )
      setRecords(result.content)
      setTotalPages(result.totalPages)
    } catch (error) {
      notify("Failed to load speaking sessions", "error")
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Speaking Management"
        description="Monitor and analyze student speaking practice sessions."
      />
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1fr_auto_auto_auto]">
          <SearchInput
            placeholder="Filter by user email..."
            value={userId}
            onChange={(event) => {
              setUserId(event.target.value)
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
            <option>IN_PROGRESS</option>
            <option>COMPLETED</option>
            <option>CANCELLED</option>
          </Select>
        </div>
        {loading ? (
          <LoadingState />
        ) : records.length === 0 ? (
          <EmptyState
            title="No speaking sessions found"
            description="Change your filters or search terms."
          />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1200px] text-left text-sm">
                <thead className="bg-muted/70 text-[11px] uppercase text-muted-foreground">
                  <tr>
                    {[
                      "User",
                      "Topic",
                      "Level",
                      "Status",
                      "Score",
                      "Attempts",
                      "Duration",
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
                  {records.map((item) => {
                    const duration = item.completedAt 
                      ? Math.round((new Date(item.completedAt).getTime() - new Date(item.startedAt).getTime()) / 60000)
                      : 0
                    return (
                      <tr key={item.id} className="hover:bg-muted/40">
                        <td className="px-4 py-4 font-bold truncate max-w-[180px]">
                          {item.userEmail}
                        </td>
                        <td className="px-4 py-4 truncate">{item.topic}</td>
                        <td className="px-4 py-4">
                          <Badge tone={getLevelColor(item.level)}>{item.level}</Badge>
                        </td>
                        <td className="px-4 py-4">
                          <Badge tone={getStatusColor(item.status)}>
                            {item.status}
                          </Badge>
                        </td>
                        <td className="px-4 py-4 text-center font-mono font-bold">
                          {item.overallScore.toFixed(1)}
                        </td>
                        <td className="px-4 py-4 text-center">
                          {item.attemptCount}
                        </td>
                        <td className="px-4 py-4 text-center">
                          {duration > 0 ? `${duration}m` : "—"}
                        </td>
                        <td className="px-4 py-4 text-muted-foreground text-xs">
                          {new Date(item.createdAt).toLocaleDateString()}
                        </td>
                        <td className="px-4 py-4">
                          <Button
                            variant="ghost"
                            className="size-8 min-h-8 px-0"
                            onClick={() =>
                              navigate(`/admin/speaking/${item.id}`)
                            }
                          >
                            <Eye className="size-4" />
                          </Button>
                        </td>
                      </tr>
                    )
                  })}
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
