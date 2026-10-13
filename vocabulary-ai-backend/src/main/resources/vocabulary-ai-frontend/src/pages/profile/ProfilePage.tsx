import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useAuth } from "@/hooks/useAuth";
import { SkeletonPage } from "@/components/common/Skeleton";

export function ProfilePage() {
  const navigate = useNavigate();
  const { user, isLoading, error } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    username: user?.username || "",
    email: user?.email || "",
  });
  const [localError, setLocalError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  if (isLoading) {
    return <SkeletonPage />;
  }

  if (!user) {
    return (
      <div className="page">
        <div style={{ textAlign: "center", padding: "40px" }}>
          <p>Please log in to view your profile</p>
          <Button onClick={() => navigate("/login")}>Go to Login</Button>
        </div>
      </div>
    );
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setLocalError(null);
    setSuccessMessage(null);

    // TODO: Implement API call to update user profile
    try {
      // const response = await userService.updateProfile(formData);
      // Update auth store with new user data
      setSuccessMessage("Profile updated successfully!");
      setIsEditing(false);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "Failed to update profile");
    }
  };

  const handleCancel = () => {
    setFormData({
      username: user?.username || "",
      email: user?.email || "",
    });
    setIsEditing(false);
    setLocalError(null);
  };

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">ACCOUNT</p>
          <h1>My Profile</h1>
          <p>View and manage your profile information</p>
        </div>
      </div>

      <div style={{ maxWidth: "600px", margin: "0 auto" }}>
        {/* Error/Success Messages */}
        {(error || localError) && (
          <div
            style={{
              padding: "12px 16px",
              backgroundColor: "#ffebee",
              color: "#d32f2f",
              borderRadius: "6px",
              marginBottom: "20px",
              display: "flex",
              alignItems: "center",
              gap: "10px",
            }}
          >
            <Icon name="alert" />
            {error || localError}
          </div>
        )}

        {successMessage && (
          <div
            style={{
              padding: "12px 16px",
              backgroundColor: "#e8f5e9",
              color: "#2e7d32",
              borderRadius: "6px",
              marginBottom: "20px",
              display: "flex",
              alignItems: "center",
              gap: "10px",
            }}
          >
            <Icon name="check" />
            {successMessage}
          </div>
        )}

        {/* Profile Card */}
        <div
          style={{
            backgroundColor: "white",
            borderRadius: "12px",
            padding: "30px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
          }}
        >
          {/* Avatar Section */}
          <div style={{ textAlign: "center", marginBottom: "30px" }}>
            <div
              style={{
                width: "80px",
                height: "80px",
                borderRadius: "50%",
                backgroundColor: "#e3f2fd",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "32px",
                fontWeight: "600",
                color: "#1976d2",
                margin: "0 auto",
              }}
            >
              {user.username?.slice(0, 2).toUpperCase()}
            </div>
            <p style={{ marginTop: "12px", color: "#666", fontSize: "12px" }}>
              Member since 2024
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSave}>
            <div style={{ marginBottom: "20px" }}>
              <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                Full Name
              </label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                disabled={!isEditing}
                style={{
                  width: "100%",
                  padding: "10px 12px",
                  border: "1px solid #e0e0e0",
                  borderRadius: "6px",
                  fontSize: "14px",
                  backgroundColor: !isEditing ? "#f9f9f9" : "white",
                  cursor: !isEditing ? "not-allowed" : "text",
                  opacity: !isEditing ? 0.7 : 1,
                }}
              />
            </div>

            <div style={{ marginBottom: "20px" }}>
              <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                disabled={!isEditing}
                style={{
                  width: "100%",
                  padding: "10px 12px",
                  border: "1px solid #e0e0e0",
                  borderRadius: "6px",
                  fontSize: "14px",
                  backgroundColor: !isEditing ? "#f9f9f9" : "white",
                  cursor: !isEditing ? "not-allowed" : "text",
                  opacity: !isEditing ? 0.7 : 1,
                }}
              />
            </div>

            {/* User Role */}
            <div style={{ marginBottom: "20px" }}>
              <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                Role
              </label>
              <div
                style={{
                  padding: "10px 12px",
                  backgroundColor: "#f0f0f0",
                  borderRadius: "6px",
                  color: "#666",
                  fontSize: "14px",
                  textTransform: "capitalize",
                }}
              >
                {user.role}
              </div>
            </div>

            {/* User ID */}
            <div style={{ marginBottom: "30px" }}>
              <label style={{ display: "block", marginBottom: "8px", fontWeight: "500", fontSize: "12px", color: "#999" }}>
                User ID
              </label>
              <div
                style={{
                  padding: "10px 12px",
                  backgroundColor: "#f9f9f9",
                  borderRadius: "6px",
                  color: "#999",
                  fontSize: "12px",
                  fontFamily: "monospace",
                  wordBreak: "break-all",
                }}
              >
                {user.id}
              </div>
            </div>

            {/* Actions */}
            <div style={{ display: "flex", gap: "10px" }}>
              {!isEditing ? (
                <Button onClick={() => setIsEditing(true)} style={{ flex: 1 }}>
                  <Icon name="edit" /> Edit Profile
                </Button>
              ) : (
                <>
                  <Button type="submit" style={{ flex: 1 }} disabled={isLoading}>
                    {isLoading ? "Saving..." : "Save Changes"}
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleCancel}
                    style={{ flex: 1 }}
                    disabled={isLoading}
                  >
                    Cancel
                  </Button>
                </>
              )}
            </div>
          </form>

          {/* Danger Zone */}
          <div
            style={{
              marginTop: "30px",
              paddingTop: "20px",
              borderTop: "1px solid #e0e0e0",
            }}
          >
            <h3 style={{ fontSize: "14px", fontWeight: "600", color: "#d32f2f", marginBottom: "15px" }}>
              <Icon name="alert" /> Danger Zone
            </h3>
            <Button
              variant="secondary"
              onClick={() => navigate("/profile/change-password")}
              style={{ width: "100%", marginBottom: "10px" }}
            >
              <Icon name="lock" /> Change Password
            </Button>
            <Button
              variant="secondary"
              onClick={() => {
                if (confirm("Are you sure? This action cannot be undone.")) {
                  // TODO: Implement delete account
                  alert("Delete account functionality coming soon");
                }
              }}
              style={{ width: "100%", color: "#d32f2f" }}
            >
              <Icon name="trash" /> Delete Account
            </Button>
          </div>
        </div>

        {/* Back Button */}
        <Button
          variant="secondary"
          onClick={() => navigate("/dashboard")}
          style={{ marginTop: "20px" }}
        >
          ← Back to Dashboard
        </Button>
      </div>
    </div>
  );
}
