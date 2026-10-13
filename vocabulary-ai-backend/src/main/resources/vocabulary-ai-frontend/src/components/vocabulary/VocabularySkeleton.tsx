import { Skeleton } from "@/components/common/Skeleton";
import { Card } from "@/components/common/Card";

/**
 * VocabularySkeleton Component
 * 
 * Displays skeleton loaders for vocabulary page
 * while word data is being fetched from backend APIs
 */
export function VocabularySkeleton() {
  return (
    <div className="page">
      {/* Page Heading Skeleton */}
      <div className="page-heading">
        <div>
          <p className="eyebrow">
            <Skeleton width="150px" height="14px" />
          </p>
          <h1 style={{ marginTop: "8px", marginBottom: "8px" }}>
            <Skeleton width="300px" height="32px" />
          </h1>
          <p>
            <Skeleton width="400px" height="16px" />
          </p>
        </div>
        <Skeleton width="140px" height="36px" />
      </div>

      {/* Filter Card Skeleton */}
      <Card className="filter-card">
        <div style={{ marginBottom: "16px" }}>
          <Skeleton width="100%" height="40px" />
        </div>
        <div className="filter-row">
          <Skeleton width="60px" height="16px" style={{ marginBottom: "8px" }} />
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <Skeleton key={`level-${i}`} width="50px" height="28px" />
            ))}
          </div>
        </div>
      </Card>

      {/* Results Heading Skeleton */}
      <div className="results-heading">
        <p>
          <Skeleton width="150px" height="16px" />
        </p>
        <Skeleton width="200px" height="16px" />
      </div>

      {/* Vocabulary Grid Skeleton */}
      <div className="vocab-grid">
        {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((i) => (
          <Card key={`vocab-${i}`} className="vocab-card">
            <div className="vocab-top">
              <Skeleton width="50px" height="20px" />
              <Skeleton circle width="24px" height="24px" />
            </div>
            <Skeleton width="80%" height="20px" style={{ marginBottom: "8px", marginTop: "12px" }} />
            <Skeleton width="60%" height="14px" style={{ marginBottom: "12px" }} />
            <Skeleton width="100%" height="40px" style={{ marginBottom: "12px" }} />
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <Skeleton width="120px" height="14px" />
              <Skeleton width="100px" height="28px" />
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
