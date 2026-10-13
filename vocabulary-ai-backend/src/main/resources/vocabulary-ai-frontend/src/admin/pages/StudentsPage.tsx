import {
  Ban,
  Eye,
  MoreHorizontal,
  UserCheck,
  UserMinus,
  Users,
} from "lucide-react"
import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router"
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts"
import ChartCard from "../components/common/ChartCard"
import PageHeader from "../components/common/PageHeader"
import StatCard from "../components/common/StatCard"
import {
  Badge,
  Button,
  Card,
  EmptyState,
  Modal,
  Pagination,
  SearchInput,
  Select,
} from "../components/common/ui"
import { growthData } from "../data/platform"
import { studentService, type StudentListItem } from "../services/studentService"
import { useAppStore } from "../store/useAppStore"

export function StudentDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const studentDetail = useAppStore((state) => state.studentDetail)
  const setStudentDetail = useAppStore((state) => state.setStudentDetail)

  useEffect(() => {
    if (id) {
      setLoading(true)
      setError(null)
      studentService
        .getDetail(Number(id))
        .then((detail) => {
          setStudentDetail(detail)
        })
        .catch((err) => {
          setError(err instanceof Error ? err.message : "Failed to load student")
        })
        .finally(() => setLoading(false))
    }
  }, [id, setStudentDetail])

  if (loading) {
    return (
      <Card>
        <EmptyState
          title="Loading..."
          description="Fetching student details from server"
        />
      </Card>
    )
  }

  if (error || !studentDetail) {
    return (
      <Card>
        <EmptyState
          title="Student not found"
          description={error || "The requested student account does not exist."}
        />
      </Card>
    )
  }

  const student = studentDetail
  const metrics = [
    ["Current Level", "B1"],
    ["Words Learned", student.wordsLearned.toLocaleString()],
    ["Sessions", student.totalSessions.toString()],
    ["Accuracy", `${student.accuracyRate.toFixed(1)}%`],
    ["Total Paid", `₫${student.totalPaid.toLocaleString()}`],
    ["Payments", student.paymentCount.toString()],
    ["Remaining Days", student.remainingDays.toString()],
  ]

  return (
    <>
      <PageHeader
        title={student.fullName}
        description={`@${student.username} · ${student.email}`}
        actions={
          <Button
            variant="secondary"
            onClick={() => navigate("/admin/students")}
          >
            Back to students
          </Button>
        }
      />
      <Card className="mb-6 p-6">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
          <div className="flex size-20 items-center justify-center rounded-2xl bg-primary text-xl font-bold text-white">
            {student.fullName.charAt(0)}
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 text-xl font-bold">
              {student.fullName}
              <Badge tone={student.status === "ACTIVE" ? "success" : "danger"}>
                {student.status}
              </Badge>
            </div>
            <div className="mt-1 text-sm text-muted-foreground">
              Joined {new Date(student.createdAt).toLocaleDateString()} · {student.totalSessions} sessions
            </div>
          </div>
        </div>
      </Card>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {metrics.map(([label, value]) => (
          <Card key={label} className="p-5">
            <div className="text-sm text-muted-foreground">{label}</div>
            <div className="mt-2 text-2xl font-extrabold">{value}</div>
          </Card>
        ))}
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-[1.4fr_1fr]">
        <ChartCard
          title="Learning Activity"
          description="Sessions over the last 6 months"
        >
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={growthData}>
                <CartesianGrid vertical={false} stroke="var(--border)" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip
                  contentStyle={{
                    background: "var(--card)",
                    borderColor: "var(--border)",
                  }}
                />
                <Area
                  dataKey="activeStudents"
                  stroke="var(--primary)"
                  fill="var(--primary-soft)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </ChartCard>
        <Card className="p-5">
          <div className="font-bold">Current Subscription</div>
          <div className="mt-5 rounded-xl bg-primary-soft p-5">
            <Badge tone={student.hasActiveSubscription ? "success" : "warning"}>
              {student.hasActiveSubscription ? "Active" : "Inactive"}
            </Badge>
            <div className="mt-3 text-lg font-bold">{student.packageName || "No active subscription"}</div>
            {student.subscriptionEndAt && (
              <div className="mt-1 text-sm text-muted-foreground">
                Ends {new Date(student.subscriptionEndAt).toLocaleDateString()}
              </div>
            )}
          </div>
          <div className="mt-6 font-bold">Learning Stats</div>
          <div className="mt-3 space-y-3 text-sm">
            <div className="flex justify-between">
              <span>Total Sessions:</span>
              <span className="font-semibold">{student.totalSessions}</span>
            </div>
            <div className="flex justify-between">
              <span>Words Learned:</span>
              <span className="font-semibold">{student.wordsLearned.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span>Accuracy Rate:</span>
              <span className="font-semibold">{student.accuracyRate.toFixed(1)}%</span>
            </div>
          </div>
        </Card>
      </div>
    </>
  )
}

export default function StudentsPage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState("")
  const [status, setStatus] = useState<string>("All")
  const [page, setPage] = useState(1)
  const [selected, setSelected] = useState<StudentListItem | null>(null)

  const navigate = useNavigate()
  const notify = useAppStore((state) => state.notify)

  // Get data from store
  const studentList = useAppStore((state) => state.studentList)
  const studentStats = useAppStore((state) => state.studentStats)
  const setStudentList = useAppStore((state) => state.setStudentList)
  const setStudentStats = useAppStore((state) => state.setStudentStats)

  // Load students on mount
  useEffect(() => {
    setLoading(true)
    setError(null)
    studentService
      .list(0, 20, "createdAt,desc", status !== "All" ? status : undefined, query || undefined)
      .then((data) => setStudentList(data))
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load students"))
      .finally(() => setLoading(false))

    // Load stats
    studentService
      .getStats()
      .then((stats) => setStudentStats(stats))
      .catch(() => {}) // Ignore stats errors
  }, [setStudentList, setStudentStats])

  const handleSearch = async (newQuery: string) => {
    setQuery(newQuery)
    setPage(1)
    try {
      const data = await studentService.list(0, 20, "createdAt,desc", status !== "All" ? status : undefined, newQuery || undefined)
      setStudentList(data)
    } catch (err) {
      notify("Search failed", "error")
    }
  }

  const handleStatusFilter = async (newStatus: string) => {
    setStatus(newStatus)
    setPage(1)
    try {
      const data = await studentService.list(0, 20, "createdAt,desc", newStatus !== "All" ? newStatus : undefined, query || undefined)
      setStudentList(data)
    } catch (err) {
      notify("Filter failed", "error")
    }
  }

  const updateStatus = async (student: StudentListItem, newStatus: string) => {
    try {
      await studentService.updateStatus(student.userId, newStatus)
      setSelected(null)
      notify(`Student status changed to ${newStatus}.`, "success")
    } catch (err) {
      notify("Failed to update status", "error")
    }
  }

  if (loading && !studentList) {
    return (
      <Card>
        <EmptyState
          title="Loading..."
          description="Fetching student data from server"
        />
      </Card>
    )
  }

  if (error && !studentList) {
    return (
      <Card>
        <EmptyState
          title="Error loading students"
          description={error}
        />
      </Card>
    )
  }

  const stats = studentStats || {
    totalStudents: 0,
    activeStudents: 0,
    inactiveStudents: 0,
    bannedStudents: 0,
  }

  const students = studentList?.content || []
  const pages = studentList?.totalPages || 1

  return (
    <>
      <PageHeader
        title="Student Management"
        description="Manage learner accounts, access and subscription status."
      />
      <div className="mb-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Total Students"
          value={stats.totalStudents.toLocaleString()}
          icon={Users}
        />
        <StatCard
          label="Active Students"
          value={stats.activeStudents.toLocaleString()}
          icon={UserCheck}
          tone="success"
        />
        <StatCard
          label="Inactive Students"
          value={stats.inactiveStudents.toLocaleString()}
          icon={UserMinus}
          tone="warning"
        />
        <StatCard
          label="Banned Students"
          value={stats.bannedStudents.toLocaleString()}
          icon={Ban}
          tone="info"
        />
      </div>
      <Card className="overflow-hidden">
        <div className="grid gap-3 p-5 md:grid-cols-[1fr_auto]">
          <SearchInput
            placeholder="Search name, username or email..."

            value={query}
            onChange={(event) => handleSearch(event.target.value)}
          />
          <Select
            value={status}
            onChange={(event) => handleStatusFilter(event.target.value)}
          >
            <option>All</option>
            <option>ACTIVE</option>
            <option>INACTIVE</option>
            <option>BANNED</option>
          </Select>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[950px] text-left text-sm">
            <thead className="bg-muted text-[11px] uppercase text-muted-foreground">
              <tr>
                {[
                  "Student",
                  "Username",
                  "Email",
                  "Sessions",
                  "Words Learned",
                  "Status",
                  "Subscription",
                  "Actions",
                ].map((item) => (
                  <th key={item} className="px-5 py-3">
                    {item}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {students.map((student) => (
                <tr key={student.userId} className="hover:bg-muted/40">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="flex size-10 items-center justify-center rounded-full bg-primary-soft text-xs font-bold text-primary">
                        {student.fullName.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold">{student.fullName}</div>
                        <div className="text-xs text-muted-foreground">
                          {student.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-muted-foreground">
                    @{student.username}
                  </td>
                  <td className="px-5 py-4 text-sm text-muted-foreground">
                    {student.email}
                  </td>
                  <td className="px-5 py-4">
                    {student.totalSessions}
                  </td>
                  <td className="px-5 py-4">
                    -
                  </td>
                  <td className="px-5 py-4">
                    <Badge
                      tone={
                        student.status === "ACTIVE"
                          ? "success"
                          : student.status === "BANNED"
                            ? "danger"
                            : "neutral"
                      }
                    >
                      {student.status}
                    </Badge>
                  </td>
                  <td className="px-5 py-4">
                    <Badge tone={student.hasActiveSubscription ? "success" : "neutral"}>
                      {student.hasActiveSubscription ? "Active" : "None"}
                    </Badge>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        className="size-9 min-h-9 px-0"
                        onClick={() =>
                          navigate(`/admin/students/${student.userId}`)
                        }
                      >
                        <Eye className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        className="size-9 min-h-9 px-0"
                        onClick={() => setSelected(student)}
                      >
                        <MoreHorizontal className="size-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} pages={pages} onChange={setPage} />
      </Card>
      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title={`Manage ${selected?.fullName ?? "student"}`}
      >
        <div className="grid gap-3">
          <Button
            variant="secondary"
            onClick={() => selected && updateStatus(selected, "ACTIVE")}
          >
            <UserCheck className="size-4" />
            Activate account
          </Button>
          <Button
            variant="secondary"
            onClick={() => selected && updateStatus(selected, "INACTIVE")}
          >
            <UserMinus className="size-4" />
            Deactivate account
          </Button>
          <Button
            variant="danger"
            onClick={() => selected && updateStatus(selected, "BANNED")}
          >
            <Ban className="size-4" />
            Ban account
          </Button>
        </div>
      </Modal>
    </>
  )
}
