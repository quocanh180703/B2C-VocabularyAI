import { Outlet } from "react-router"
import { useAppStore } from "../../store/useAppStore"
import ToastContainer from "../common/Toast"
import Header from "./Header"
import Sidebar from "./Sidebar"

export default function AdminLayout() {
  const theme = useAppStore((state) => state.theme)
  const dark =
    theme === "dark" ||
    (theme === "system" &&
      window.matchMedia("(prefers-color-scheme: dark)").matches)
  return (
    <div className={dark ? "dark" : ""}>
      <div className="flex min-h-screen bg-background text-foreground">
        <Sidebar />
        <Sidebar mobile />
        <div className="min-w-0 flex-1">
          <Header />
          <main className="p-4 sm:p-6 lg:p-8">
            <Outlet />
          </main>
        </div>
        <ToastContainer />
      </div>
    </div>
  )
}
