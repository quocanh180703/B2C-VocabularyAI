import {
  BrainCircuit,
  CheckCircle2,
  Clock3,
  FileCheck2,
  Gauge,
  Sparkles,
  Target,
  TimerReset,
  TriangleAlert,
  Users,
} from "lucide-react"
import { useEffect, useState } from "react"
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts"
import ChartCard from "../components/common/ChartCard"
import ManagementPage from "../components/common/ManagementPage"
import PageHeader from "../components/common/PageHeader"
import StatCard from "../components/common/StatCard"
import {
  Badge,
  Button,
  Card,
  Input,
  Modal,
  Select,
  Textarea,
} from "../components/common/ui"
import { genericData, growthData } from "../data/platform"
import { paymentService } from "../services/paymentService"
import { subscriptionService } from "../services/subscriptionService"
import { useAppStore } from "../store/useAppStore"

export function LearningProgressPage() {
  return (
    <ManagementPage
      title="Learning Progress"
      description="Track spaced repetition, mastery and review performance."
      rows={genericData.progress}
      columns={[
        { key: "student", label: "Student" },
        { key: "learned", label: "Words Learned" },
        { key: "reviewed", label: "Reviewed", hideMobile: true },
        { key: "mastered", label: "Mastered" },
        { key: "accuracy", label: "Accuracy" },
        { key: "streak", label: "Streak" },
        { key: "activity", label: "Last Activity", hideMobile: true },
        { key: "status", label: "Status" },
      ]}
      stats={[
        { label: "Total Words Learned", value: "148,320" },
        { label: "Due for Review", value: "24,892" },
        { label: "Mastered Words", value: "96,480" },
        { label: "Review Accuracy", value: "86.4%" },
      ]}
    />
  )
}
export function QuizPage() {
  return (
    <ManagementPage
      title="Quiz Management"
      description="Review attempts, scores, accuracy and answer details."
      rows={genericData.quiz}
      columns={[
        { key: "quiz", label: "Quiz" },
        { key: "student", label: "Student" },
        { key: "questions", label: "Questions" },
        { key: "score", label: "Score" },
        { key: "accuracy", label: "Accuracy" },
        { key: "time", label: "Time", hideMobile: true },
        { key: "completed", label: "Completed At", hideMobile: true },
        { key: "status", label: "Status" },
      ]}
      stats={[
        { label: "Total Quiz Attempts", value: "32,890" },
        { label: "Average Score", value: "82.6%" },
        { label: "Pass Rate", value: "88.4%" },
        { label: "Avg. Completion Time", value: "08:42" },
      ]}
    />
  )
}
export function QuizDetailPage() {
  return (
    <ManagementPage
      title="Quiz Attempt Details"
      description="Question-by-question answer review and AI weakness analysis."
      rows={[
        {
          id: 1,
          question: "Choose the synonym of “benefit”",
          answer: "advantage",
          correct: "advantage",
          result: "Correct",
        },
        {
          id: 2,
          question: "Complete: We should ___ the environment.",
          answer: "protect",
          correct: "protect",
          result: "Correct",
        },
        {
          id: 3,
          question: "Meaning of “abandon”",
          answer: "continue",
          correct: "leave permanently",
          result: "Incorrect",
        },
      ]}
      columns={[
        { key: "question", label: "Question" },
        { key: "answer", label: "Student Answer" },
        { key: "correct", label: "Correct Answer" },
        { key: "result", label: "Result" },
      ]}
    />
  )
}
export function WritingPage() {
  return (
    <ManagementPage
      title="Writing Management"
      description="Review AI corrections, grammar and vocabulary feedback."
      rows={genericData.writing}
      columns={[
        { key: "student", label: "Student" },
        { key: "topic", label: "Topic" },
        { key: "original", label: "Original Text", hideMobile: true },
        { key: "correction", label: "AI Correction Status" },
        { key: "score", label: "Score" },
        { key: "created", label: "Created At" },
      ]}
      stats={[
        { label: "Total Attempts", value: "8,240" },
        { label: "Average Score", value: "81.8" },
        { label: "Grammar Score", value: "79.4" },
        { label: "Vocabulary Score", value: "84.2" },
      ]}
    />
  )
}
export function SpeakingPage() {
  return (
    <ManagementPage
      title="Speaking Management"
      description="Analyze pronunciation, fluency, grammar and AI feedback."
      rows={genericData.speaking}
      columns={[
        { key: "student", label: "Student" },
        { key: "topic", label: "Topic" },
        { key: "transcript", label: "Transcript", hideMobile: true },
        { key: "pronunciation", label: "Pronunciation" },
        { key: "fluency", label: "Fluency" },
        { key: "grammar", label: "Grammar" },
        { key: "overall", label: "Overall" },
        { key: "created", label: "Created At", hideMobile: true },
      ]}
      stats={[
        { label: "Total Attempts", value: "6,410" },
        { label: "Avg. Pronunciation", value: "84.8" },
        { label: "Avg. Fluency", value: "81.2" },
        { label: "Avg. Overall", value: "83.6" },
      ]}
    />
  )
}
function AiOverview({ analytics = false }: { analytics?: boolean }) {
  const [feature, setFeature] = useState("All Features")
  return (
    <>
      <PageHeader
        title={analytics ? "AI Analytics" : "Gemini AI Usage"}
        description={
          analytics
            ? "Analyze AI quality, latency, errors and feature adoption."
            : "Monitor Gemini requests, reliability and token consumption."
        }
        actions={
          <Select
            value={feature}
            onChange={(event) => setFeature(event.target.value)}
          >
            <option>All Features</option>
            <option>Vocabulary Explanation</option>
            <option>Learning Hint</option>
            <option>Grammar Correction</option>
            <option>Writing Evaluation</option>
            <option>Speaking Analysis</option>
          </Select>
        }
      />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <StatCard
          label="Total AI Requests"
          value="18,420"
          change="+21.3%"
          icon={BrainCircuit}
        />
        <StatCard
          label="Successful Requests"
          value="17,982"
          change="+2.4%"
          icon={CheckCircle2}
          tone="success"
        />
        <StatCard
          label="Failed Requests"
          value="438"
          change="-8.2%"
          icon={TriangleAlert}
          tone="warning"
        />
        <StatCard
          label="Avg. Response Time"
          value="1.42s"
          change="-4.8%"
          icon={Clock3}
          tone="info"
        />
        <StatCard
          label="Token Usage"
          value="4.82M"
          change="+18.7%"
          icon={Sparkles}
        />
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-2">
        <ChartCard
          title="AI Requests Over Time"
          description={`${feature} · requests and success rate`}
        >
          <div className="h-72">
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
                  dataKey="aiRequests"
                  stroke="var(--primary)"
                  fill="var(--primary-soft)"
                  strokeWidth={3}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </ChartCard>
        <ChartCard
          title="Requests by Feature"
          description="Gemini workload distribution"
        >
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={[
                  { name: "Vocabulary", value: 6200 },
                  { name: "Hints", value: 4100 },
                  { name: "Writing", value: 3280 },
                  { name: "Speaking", value: 2640 },
                  { name: "Chat", value: 2200 },
                ]}
              >
                <CartesianGrid vertical={false} stroke="var(--border)" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip
                  contentStyle={{
                    background: "var(--card)",
                    borderColor: "var(--border)",
                  }}
                />
                <Bar
                  dataKey="value"
                  fill="var(--primary)"
                  radius={[8, 8, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </ChartCard>
      </div>
    </>
  )
}
export function AiUsagePage() {
  return <AiOverview />
}
export function AiAnalyticsPage() {
  return <AiOverview analytics />
}
export function AiPromptsPage() {
  const [selected, setSelected] =
    useState<typeof genericData.prompts[number] | null>(null)
  const [name, setName] = useState("")
  const [version, setVersion] = useState("v1.0")
  const [systemPrompt, setSystemPrompt] = useState(
    "You are an expert English learning assistant aligned with CEFR standards.",
  )
  const [userPrompt, setUserPrompt] = useState(
    "Explain {{word}} at {{cefrLevel}} level. Meaning: {{meaning}}",
  )
  const notify = useAppStore((state) => state.notify)
  const edit = (prompt: typeof genericData.prompts[number]) => {
    setSelected(prompt)
    setName(String(prompt.name))
    setVersion(String(prompt.version))
  }
  return (
    <>
      <PageHeader
        title="AI Prompt Management"
        description="Version and configure Gemini prompts without changing business logic."
        actions={
          <Button
            onClick={() => {
              setSelected({
                id: 0,
                name: "",
                feature: "Vocabulary",
                version: "v1.0",
                status: "Draft",
                updated: "Now",
              })
              setName("")
              setVersion("v1.0")
            }}
          >
            Create Prompt
          </Button>
        }
      />
      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[700px] text-left text-sm">
            <thead className="bg-muted text-[11px] uppercase text-muted-foreground">
              <tr>
                <th className="px-5 py-3">Prompt Name</th>
                <th className="px-5 py-3">Feature</th>
                <th className="px-5 py-3">Version</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3">Updated At</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {genericData.prompts.map((prompt) => (
                <tr key={prompt.id}>
                  <td className="px-5 py-4 font-semibold">{prompt.name}</td>
                  <td className="px-5 py-4 text-muted-foreground">
                    {prompt.feature}
                  </td>
                  <td className="px-5 py-4">{prompt.version}</td>
                  <td className="px-5 py-4">
                    <Badge
                      tone={prompt.status === "Active" ? "success" : "neutral"}
                    >
                      {prompt.status}
                    </Badge>
                  </td>
                  <td className="px-5 py-4 text-muted-foreground">
                    {prompt.updated}
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex justify-end gap-2">
                      <Button variant="secondary" onClick={() => edit(prompt)}>
                        View / Edit
                      </Button>
                      <Button
                        variant="ghost"
                        onClick={() =>
                          notify(
                            prompt.status === "Active"
                              ? "Prompt deactivated."
                              : "Prompt activated.",
                            "info",
                          )
                        }
                      >
                        {prompt.status === "Active" ? "Deactivate" : "Activate"}
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title={selected?.id === 0 ? "Create AI prompt" : "Prompt editor"}
        wide
        footer={
          <>
            <Button variant="secondary" onClick={() => setSelected(null)}>
              Cancel
            </Button>
            <Button
              onClick={() => {
                if (!name.trim()) {
                  notify("Prompt name is required.", "error")
                  return
                }
                setSelected(null)
                notify("AI prompt saved successfully.")
              }}
            >
              Save prompt
            </Button>
          </>
        }
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="text-sm font-semibold">
            Prompt Name
            <Input
              className="mt-2"
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </label>
          <label className="text-sm font-semibold">
            Version
            <Input
              className="mt-2"
              value={version}
              onChange={(event) => setVersion(event.target.value)}
            />
          </label>
          <label className="text-sm font-semibold sm:col-span-2">
            System Prompt
            <Textarea
              className="mt-2 min-h-32 font-mono text-xs"
              value={systemPrompt}
              onChange={(event) => setSystemPrompt(event.target.value)}
            />
          </label>
          <label className="text-sm font-semibold sm:col-span-2">
            User Prompt Template
            <Textarea
              className="mt-2 min-h-40 font-mono text-xs"
              value={userPrompt}
              onChange={(event) => setUserPrompt(event.target.value)}
            />
          </label>
          <div className="sm:col-span-2">
            <div className="text-sm font-semibold">Available Variables</div>
            <div className="mt-2 flex flex-wrap gap-2">
              {[
                "{{word}}",
                "{{meaning}}",
                "{{cefrLevel}}",
                "{{userAnswer}}",
                "{{correctAnswer}}",
              ].map((variable) => (
                <Button
                  key={variable}
                  variant="secondary"
                  className="font-mono text-xs"
                  onClick={() => setUserPrompt(`${userPrompt} ${variable}`)}
                >
                  {variable}
                </Button>
              ))}
            </div>
          </div>
          <label className="text-sm font-semibold">
            Status
            <Select className="mt-2 w-full">
              <option>Active</option>
              <option>Inactive</option>
              <option>Draft</option>
            </Select>
          </label>
        </div>
      </Modal>
    </>
  )
}
export function AdminsPage() {
  return (
    <ManagementPage
      title="Administrator Management"
      description="Manage accounts with the ADMIN role."
      rows={genericData.admins}
      columns={[
        { key: "name", label: "Name" },
        { key: "username", label: "Username" },
        { key: "email", label: "Email" },
        { key: "status", label: "Status" },
        { key: "created", label: "Created At" },
        { key: "login", label: "Last Login" },
      ]}
      allowCreate
      primaryAction="Add Administrator"
    />
  )
}
export function PackagesPage() {
  return (
    <ManagementPage
      title="Subscription Packages"
      description="Create and manage VocabularyAI access packages."
      rows={genericData.packages}
      columns={[
        { key: "name", label: "Package Name" },
        { key: "duration", label: "Duration" },
        { key: "price", label: "Price" },
        { key: "features", label: "Features", hideMobile: true },
        { key: "subscribers", label: "Subscribers" },
        { key: "status", label: "Status" },
        { key: "created", label: "Created At", hideMobile: true },
      ]}
      allowCreate
      primaryAction="Create Package"
    />
  )
}
export function SubscriptionsPage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [rows, setRows] = useState<any[]>([])
  const [stats, setStats] = useState({
    active: 0,
    expired: 0,
    pending: 0,
    renewal: "0%",
  })

  useEffect(() => {
    setLoading(true)
    setError(null)
    subscriptionService
      .list(0, 100)
      .then((data) => {
        setRows(
          data.content.map((sub) => ({
            id: sub.subscriptionId,
            student: sub.userFullName,
            email: sub.userEmail,
            package: sub.packageName,
            start: new Date(sub.startAt).toLocaleDateString(),
            end: new Date(sub.endAt).toLocaleDateString(),
            amount: `₫${sub.amount.toLocaleString()}`,
            status: sub.status,
          }))
        )
        // Simple stat calculation
        const active = data.content.filter((s) => s.status === "ACTIVE").length
        setStats({
          active,
          expired: data.totalElements - active,
          pending: 0,
          renewal: "78.6%",
        })
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "Failed to load subscriptions")
      })
      .finally(() => setLoading(false))
  }, [])

  if (error) {
    return (
      <ManagementPage
        title="Subscriptions"
        description={`Error: ${error}`}
        rows={[]}
        columns={[]}
        stats={[]}
      />
    )
  }

  return (
    <ManagementPage
      title="Subscriptions"
      description={loading ? "Loading..." : "Track active, expired, cancelled and pending subscriptions."}
      rows={rows}
      columns={[
        { key: "student", label: "Student" },
        { key: "package", label: "Package" },
        { key: "start", label: "Start Date" },
        { key: "end", label: "End Date" },
        { key: "amount", label: "Amount" },
        { key: "status", label: "Status" },
      ]}
      stats={[
        { label: "Active Subscriptions", value: stats.active.toString() },
        { label: "Expired", value: stats.expired.toString() },
        { label: "Renewal Rate", value: stats.renewal },
      ]}
    />
  )
}

export function PaymentsPage() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [rows, setRows] = useState<any[]>([])
  const [stats, setStats] = useState({
    revenue: "₫0",
    successful: 0,
    pending: 0,
    failed: 0,
  })

  useEffect(() => {
    setLoading(true)
    setError(null)
    paymentService
      .list(0, 100)
      .then((data) => {
        setRows(
          data.content.map((payment) => ({
            id: payment.orderCode,
            student: payment.userFullName,
            email: payment.userEmail,
            package: payment.packageName,
            amount: `₫${payment.amount.toLocaleString()}`,
            status: payment.status,
            transaction: payment.transactionCode,
            paid: new Date(payment.paidAt).toLocaleDateString(),
            created: new Date(payment.createdAt).toLocaleDateString(),
          }))
        )
        // Calculate stats
        const totalAmount = data.content.reduce((sum, p) => sum + p.amount, 0)
        const successful = data.content.filter(
          (p) => p.status === "SUCCESS"
        ).length
        const pending = data.content.filter(
          (p) => p.status === "PENDING"
        ).length
        const failed = data.content.filter(
          (p) => p.status === "FAILED"
        ).length

        setStats({
          revenue: `₫${totalAmount.toLocaleString()}`,
          successful,
          pending,
          failed,
        })
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "Failed to load payments")
      })
      .finally(() => setLoading(false))
  }, [])

  if (error) {
    return (
      <ManagementPage
        title="Payments"
        description={`Error: ${error}`}
        rows={[]}
        columns={[]}
        stats={[]}
      />
    )
  }

  return (
    <ManagementPage
      title="Payments"
      description={loading ? "Loading..." : "Monitor PayOS payment orders."}
      rows={rows}
      columns={[
        { key: "id", label: "Order Code" },
        { key: "student", label: "Student" },
        { key: "package", label: "Package" },
        { key: "amount", label: "Amount" },
        { key: "status", label: "Status" },
        { key: "transaction", label: "Transaction Code" },
        { key: "paid", label: "Paid Date", hideMobile: true },
      ]}
      stats={[
        { label: "Total Revenue", value: stats.revenue },
        { label: "Successful", value: stats.successful.toString() },
        { label: "Pending", value: stats.pending.toString() },
        { label: "Failed", value: stats.failed.toString() },
      ]}
    />
  )
}
export function ReportsPage() {
  return (
    <>
      <ManagementPage
        title="Reports"
        description="Export learning, business and AI performance reports."
        rows={[
          {
            id: 1,
            report: "Student Growth",
            category: "Learning Report",
            period: "May 2025",
            updated: "Today",
          },
          {
            id: 2,
            report: "Vocabulary Growth",
            category: "Learning Report",
            period: "May 2025",
            updated: "Today",
          },
          {
            id: 3,
            report: "Revenue & Payments",
            category: "Business Report",
            period: "May 2025",
            updated: "1 day ago",
          },
          {
            id: 4,
            report: "AI Errors & Token Usage",
            category: "AI Report",
            period: "May 2025",
            updated: "2 hours ago",
          },
        ]}
        columns={[
          { key: "report", label: "Report" },
          { key: "category", label: "Category" },
          { key: "period", label: "Date Range" },
          { key: "updated", label: "Last Updated" },
        ]}
      />
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {[
          {
            title: "Learning Report",
            icon: Target,
            text: "Student growth, vocabulary, quiz, writing and speaking.",
          },
          {
            title: "Business Report",
            icon: FileCheck2,
            text: "Revenue, subscriptions, payments and package performance.",
          },
          {
            title: "AI Report",
            icon: BrainCircuit,
            text: "AI usage, success rate, errors and token consumption.",
          },
        ].map(({ title, icon: Icon, text }) => (
          <Card key={title} className="p-5">
            <Icon className="size-6 text-primary" />
            <div className="mt-4 font-bold">{title}</div>
            <p className="mt-2 text-sm text-muted-foreground">{text}</p>
          </Card>
        ))}
      </div>
    </>
  )
}
