import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Icon } from "../common/Icon";
import { ThemeToggle } from "../common/ThemeToggle";
import { useAuth } from "../../hooks/useAuth";

interface TopbarProps {
  theme: "light" | "dark";
  onToggleTheme: () => void;
}

export function Topbar({ theme, onToggleTheme }: TopbarProps) {
  const navigate = useNavigate();
  const { user, logout, isLoading } = useAuth();
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
      setShowProfileMenu(false);
      navigate("/login");
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  const handleProfile = () => {
    setShowProfileMenu(false);
    navigate("/profile");
  };

  return (
    <header className="topbar">
      <div className="global-search">
        <Icon name="search" />
        <input placeholder="Search..." />
      </div>

      <div className="top-actions">
        <div className="notification">
          <button className="icon-button">
            <Icon name="bell" />
            <i></i>
          </button>
        </div>

        <ThemeToggle theme={theme} toggle={onToggleTheme} />

        <div className="header-divider"></div>

        {/* Profile Menu */}
        <div style={{ position: "relative" }}>
          <button
            className="header-profile"
            onClick={() => setShowProfileMenu(!showProfileMenu)}
            style={{ cursor: "pointer", backgroundColor: "transparent", border: "none", display: "flex", alignItems: "center", gap: "10px" }}
          >
            <div className="avatar" style={{ minWidth: "40px" }}>
              {user?.username?.slice(0, 2).toUpperCase() || "U"}
            </div>
            <div>
              <strong>{user?.username || "User"}</strong>
              <p>{user?.role || "Student"}</p>
            </div>
            <Icon name={showProfileMenu ? "chevron-up" : "chevron-down"} />
          </button>

          {/* Dropdown Menu */}
          {showProfileMenu && (
            <div
              style={{
                position: "absolute",
                top: "100%",
                right: 0,
                backgroundColor: "white",
                border: "1px solid #e0e0e0",
                borderRadius: "8px",
                minWidth: "200px",
                marginTop: "8px",
                boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
                zIndex: 1000,
              }}
            >
              {/* Profile Info */}
              <div style={{ padding: "12px 16px", borderBottom: "1px solid #e0e0e0" }}>
                <p style={{ fontSize: "12px", color: "#666", margin: 0 }}>Logged in as</p>
                <p style={{ fontWeight: "600", margin: "4px 0 0 0" }}>{user?.email}</p>
              </div>

              {/* Menu Items */}
              <button
                onClick={handleProfile}
                style={{
                  width: "100%",
                  padding: "10px 16px",
                  textAlign: "left",
                  backgroundColor: "transparent",
                  border: "none",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  fontSize: "14px",
                  color: "#333",
                  transition: "background-color 0.2s",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = "#f5f5f5")}
                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
              >
                <Icon name="user" /> My Profile
              </button>

              <button
                onClick={() => navigate("/settings")}
                style={{
                  width: "100%",
                  padding: "10px 16px",
                  textAlign: "left",
                  backgroundColor: "transparent",
                  border: "none",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  fontSize: "14px",
                  color: "#333",
                  transition: "background-color 0.2s",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = "#f5f5f5")}
                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
                disabled={isLoading}
              >
                <Icon name="settings" /> Settings
              </button>

              {/* Divider */}
              <div style={{ height: "1px", backgroundColor: "#e0e0e0", margin: "8px 0" }} />

              {/* Logout */}
              <button
                onClick={handleLogout}
                disabled={isLoading}
                style={{
                  width: "100%",
                  padding: "10px 16px",
                  textAlign: "left",
                  backgroundColor: "transparent",
                  border: "none",
                  cursor: isLoading ? "not-allowed" : "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  fontSize: "14px",
                  color: "#d32f2f",
                  transition: "background-color 0.2s",
                  opacity: isLoading ? 0.6 : 1,
                }}
                onMouseEnter={(e) => !isLoading && (e.currentTarget.style.backgroundColor = "#ffebee")}
                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
              >
                <Icon name="logout" /> {isLoading ? "Logging out..." : "Logout"}
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
