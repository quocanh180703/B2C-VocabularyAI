import { ChevronLeft, ChevronRight, LogOut, X } from "lucide-react"
import { NavLink, useNavigate } from "react-router"
import { useAppStore } from "../../store/useAppStore"
import { Button } from "../common/ui"
import { brandIcon as BrandIcon, navigation } from "./navigation"

export default function Sidebar({ mobile = false }: { mobile?: boolean }) {
  const {
    sidebarCollapsed,
    toggleSidebar,
    mobileOpen,
    setMobileOpen,
    logout,
    user,
  } = useAppStore()
  const navigate = useNavigate()
  const collapsed = mobile ? false : sidebarCollapsed
  const signOut = () => {
    logout()
    navigate("/login")
  }
  return (
    <>
      {mobile && mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/45 backdrop-blur-sm lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}
      <aside
        className={`${
          mobile
            ? `fixed inset-y-0 left-0 z-50 w-72 transform lg:hidden ${
                mobileOpen ? "translate-x-0" : "-translate-x-full"
              }`
            : `hidden lg:flex ${collapsed ? "w-[84px]" : "w-[272px]"}`
        } flex-col border-r border-border bg-card transition-all duration-300`}
      >
        <div className="flex h-20 items-center gap-3 border-b border-border px-5">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary text-white">
            <BrandIcon className="size-5" />
          </div>
          {!collapsed && (
            <div>
              <div className="text-lg font-extrabold tracking-tight">
                VocabularyAI
              </div>
              <div className="text-[11px] font-medium text-muted-foreground">
                Admin Console
              </div>
            </div>
          )}
          {mobile && (
            <Button
              variant="ghost"
              className="ml-auto size-9 min-h-9 px-0"
              onClick={() => setMobileOpen(false)}
              aria-label="Close menu"
            >
              <X className="size-5" />
            </Button>
          )}
        </div>
        <nav className="flex-1 overflow-y-auto px-3 py-4">
          {navigation.map((section) => (
            <div key={section.group} className="mb-5">
              {!collapsed && (
                <div className="mb-2 px-3 text-[10px] font-bold uppercase tracking-[0.12em] text-muted-foreground">
                  {section.group}
                </div>
              )}
              <div className="space-y-1">
                {section.items.map(({ to, label, icon: Icon }) => (
                  <NavLink
                    key={to}
                    to={to}
                    onClick={() => setMobileOpen(false)}
                    className={({ isActive }) =>
                      `flex min-h-10 items-center gap-3 rounded-xl px-3 text-[13px] font-semibold transition ${
                        isActive
                          ? "bg-primary-soft text-primary"
                          : "text-muted-foreground hover:bg-muted hover:text-foreground"
                      } ${collapsed ? "justify-center" : ""}`
                    }
                    title={collapsed ? label : undefined}
                  >
                    <Icon className="size-[18px] shrink-0" />
                    {!collapsed && <span>{label}</span>}
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </nav>
        <div className="border-t border-border p-3">
          <div
            className={`mb-2 flex items-center gap-3 rounded-xl bg-muted p-3 ${
              collapsed ? "justify-center" : ""
            }`}
          >
            <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-bold text-white">
              {user?.avatar}
            </div>
            {!collapsed && (
              <div className="min-w-0 flex-1">
                <div className="truncate text-xs font-bold">{user?.name}</div>
                <div className="truncate text-[11px] text-muted-foreground">
                  Platform Administrator
                </div>
              </div>
            )}
          </div>
          <Button
            variant="ghost"
            className={`w-full ${collapsed ? "px-0" : "justify-start"}`}
            onClick={signOut}
          >
            <LogOut className="size-4" />
            {!collapsed && "Logout"}
          </Button>
          {!mobile && (
            <Button
              variant="ghost"
              className="mt-1 w-full"
              onClick={toggleSidebar}
            >
              {collapsed ? (
                <ChevronRight className="size-4" />
              ) : (
                <>
                  <ChevronLeft className="size-4" />
                  Collapse sidebar
                </>
              )}
            </Button>
          )}
        </div>
      </aside>
    </>
  )
}
