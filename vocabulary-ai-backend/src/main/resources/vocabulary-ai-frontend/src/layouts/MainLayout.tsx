import { ReactNode } from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";
import { NavItem, Page } from "@/types";

interface MainLayoutProps {
  children: ReactNode;
  currentPage: Page;
  onNavigate: (page: Page) => void;
  navItems: NavItem[];
  theme: "light" | "dark";
  onToggleTheme: () => void;
}

export function MainLayout({
  children,
  currentPage,
  onNavigate,
  navItems,
  theme,
  onToggleTheme,
}: MainLayoutProps) {
  return (
    <div className="app-shell">
      <Sidebar currentPage={currentPage} onNavigate={onNavigate} navItems={navItems} />
      <div className="main-shell">
        <Topbar theme={theme} onToggleTheme={onToggleTheme} />
        <main>{children}</main>
      </div>
    </div>
  );
}
