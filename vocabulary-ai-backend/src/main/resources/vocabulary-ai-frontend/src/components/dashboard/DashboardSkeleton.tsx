import { Skeleton } from "@/components/common/Skeleton";
import { Card } from "@/components/common/Card";

/**
 * DashboardSkeleton Component
 * 
 * Displays skeleton loaders for all dashboard sections
 * while data is being fetched from backend APIs
 */
export function DashboardSkeleton() {
  return (
    <div className="page">
      {/* Page Heading Skeleton */}
      <div className="page-heading">
        <div>
          <p className="eyebrow">
            <Skeleton width="120px" height="14px" />
          </p>
          <h1 style={{ marginTop: "8px", marginBottom: "8px" }}>
            <Skeleton width="300px" height="32px" />
          </h1>
          <p>
            <Skeleton width="400px" height="16px" />
          </p>
        </div>
        <div className="streak-pill" style={{ opacity: 0.5 }}>
          <Skeleton circle width="24px" height="24px" />
          <Skeleton width="60px" height="20px" />
          <Skeleton width="80px" height="14px" />
        </div>
      </div>

      {/* Stats Grid Skeleton */}
      <div className="stats-grid">
        {[1, 2, 3, 4].map((i) => (
          <Card key={`stat-${i}`} className="stat-card">
            <span className="stat-icon" style={{ opacity: 0.3 }}>
              <Skeleton circle width="40px" height="40px" />
            </span>
            <div>
              <div style={{ marginBottom: "8px" }}>
                <Skeleton width="80px" height="20px" />
              </div>
              <Skeleton width="120px" height="14px" />
            </div>
          </Card>
        ))}
      </div>

      {/* Plan Status Card Skeleton */}
      <Card style={{ padding: "20px", marginBottom: "20px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div style={{ flex: 1 }}>
            <Skeleton width="150px" height="20px" style={{ marginBottom: "10px" }} />
            <Skeleton width="300px" height="14px" />
          </div>
          <Skeleton width="100px" height="36px" />
        </div>
      </Card>

      {/* Dashboard Grid Skeleton */}
      <div className="dashboard-grid">
        {/* Review Card Skeleton */}
        <Card style={{ padding: "20px" }}>
          <Skeleton width="120px" height="20px" style={{ marginBottom: "15px" }} />
          <Skeleton width="100%" height="80px" style={{ marginBottom: "15px" }} />
          <Skeleton width="100px" height="36px" />
        </Card>

        {/* Goal Card Skeleton */}
        <Card style={{ padding: "20px" }}>
          <Skeleton width="100px" height="20px" style={{ marginBottom: "15px" }} />
          <Skeleton width="100%" height="60px" style={{ marginBottom: "15px" }} />
          <div style={{ marginTop: "15px" }}>
            {[1, 2, 3].map((i) => (
              <div key={`goal-${i}`} style={{ marginBottom: "12px" }}>
                <Skeleton width="100%" height="40px" />
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Lower Grid Skeleton */}
      <div className="lower-grid">
        {/* Recent Words Card Skeleton */}
        <Card style={{ padding: "20px" }}>
          <Skeleton width="120px" height="20px" style={{ marginBottom: "15px" }} />
          <div>
            {[1, 2, 3, 4].map((i) => (
              <div key={`word-${i}`} style={{ marginBottom: "12px", display: "flex", gap: "12px" }}>
                <Skeleton circle width="40px" height="40px" />
                <div style={{ flex: 1 }}>
                  <Skeleton width="80%" height="16px" style={{ marginBottom: "6px" }} />
                  <Skeleton width="60%" height="12px" />
                </div>
              </div>
            ))}
          </div>
          <Skeleton width="100px" height="36px" style={{ marginTop: "15px" }} />
        </Card>

        {/* Insight Card Skeleton */}
        <Card style={{ padding: "20px" }}>
          <Skeleton width="100%" height="100px" style={{ marginBottom: "15px" }} />
          <div style={{ display: "flex", gap: "8px", marginBottom: "15px", flexWrap: "wrap" }}>
            {[1, 2, 3].map((i) => (
              <Skeleton key={`tag-${i}`} width="80px" height="24px" />
            ))}
          </div>
          <Skeleton width="100px" height="36px" />
        </Card>
      </div>
    </div>
  );
}
