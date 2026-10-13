import { useNavigate } from "react-router-dom";
import { Icon } from "../common/Icon";
import { NavItem, Page } from "@/types";

interface SidebarProps {
  currentPage: Page;
  onNavigate: (page: Page) => void;
  navItems: NavItem[];
}

const PAGE_ROUTES: Record<string, string> = {
  dashboard: "/dashboard",
  vocabulary: "/vocabulary",
  learn: "/learn",
  quiz: "/quiz",
  ai: "/ai",
  speaking: "/speaking",
  progress: "/progress",
  premium: "/premium",
  settings: "/settings",
};

export function Sidebar({ currentPage, onNavigate, navItems }: SidebarProps) {
  const navigate = useNavigate();

  const handleNavigate = (pageId: string) => {
    const route = PAGE_ROUTES[pageId];
    if (route) {
      navigate(route);
    } else {
      console.warn(`No route configured for page: ${pageId}`);
    }
  };

  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark">
          <Icon name="cards" />
        </span>
        <span>
          Vocabulary<span>AI</span>
        </span>
      </div>

      <nav>
        <label className="nav-label">WORKSPACE</label>
        {navItems.slice(0, 7).map((item) => (
          <button
            key={item.id}
            className={`${currentPage === item.id ? "active" : ""}`}
            onClick={() => handleNavigate(item.id)}
          >
            <Icon name={item.icon} />
            {item.label}
          </button>
        ))}

        <label className="nav-label account-label">ACCOUNT</label>
        {navItems.slice(7).map((item) => (
          <button
            key={item.id}
            className={`${currentPage === item.id ? "active" : ""}`}
            onClick={() => handleNavigate(item.id)}
          >
            <Icon name={item.icon} />
            {item.label}
          </button>
        ))}
      </nav>

      <div className="sidebar-profile">
        <div className="avatar">AH</div>
        <div>
          <strong>Anh</strong>
          <p>anh@example.com</p>
        </div>
      </div>
    </aside>
  );
}
