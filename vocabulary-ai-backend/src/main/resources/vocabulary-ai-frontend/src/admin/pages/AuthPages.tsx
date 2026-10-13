import { BrainCircuit, LockKeyhole, ShieldX } from "lucide-react"
import { useState } from "react"
import { Navigate, Outlet, useLocation, useNavigate } from "react-router"
import { useAppStore } from "../store/useAppStore"
import { Button, Card, Input } from "../components/common/ui"

export function ProtectedRoute() {
  const { isAuthenticated, user } = useAppStore()
  const location = useLocation()
  if (!isAuthenticated)
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (user?.role !== "ADMIN") return <Navigate to="/403" replace />
  return <Outlet />
}
export function LoginPage() {
  const [email, setEmail] = useState("admin@vocabularyai.vn")
  const [password, setPassword] = useState("admin123")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const login = useAppStore((state) => state.login)
  const navigate = useNavigate()
  const submit = async () => {
    setLoading(true)
    setError("")
    const ok = await login(email, password)
    setLoading(false)
    if (ok) navigate("/admin/dashboard")
    else setError("Invalid administrator credentials.")
  }
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-5">
      <Card className="w-full max-w-md p-7">
        <div className="flex size-12 items-center justify-center rounded-xl bg-primary text-white">
          <BrainCircuit className="size-6" />
        </div>
        <div className="mt-6 text-2xl font-extrabold">
          Welcome to VocabularyAI
        </div>
        <p className="mt-2 text-sm text-muted-foreground">
          Sign in to the Admin Console with an authorized ADMIN account.
        </p>
        <div className="mt-7 space-y-4">
          <label className="block text-sm font-semibold">
            Email
            <Input
              className="mt-2"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </label>
          <label className="block text-sm font-semibold">
            Password
            <Input
              className="mt-2"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onKeyDown={(event) => event.key === "Enter" && submit()}
            />
          </label>
          {error && (
            <div className="rounded-xl bg-danger-soft p-3 text-sm text-danger">
              {error}
            </div>
          )}
          <Button className="w-full" disabled={loading} onClick={submit}>
            <LockKeyhole className="size-4" />
            {loading ? "Signing in..." : "Sign in as Administrator"}
          </Button>
        </div>
      </Card>
    </div>
  )
}
export function ForbiddenPage() {
  const navigate = useNavigate()
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-5">
      <Card className="max-w-md p-8 text-center">
        <ShieldX className="mx-auto size-12 text-danger" />
        <div className="mt-5 text-2xl font-bold">403 Forbidden</div>
        <p className="mt-2 text-sm text-muted-foreground">
          You do not have permission to access the VocabularyAI Admin Console.
        </p>
        <Button className="mt-6" onClick={() => navigate("/login")}>
          Return to login
        </Button>
      </Card>
    </div>
  )
}
