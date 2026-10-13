import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { ProgressBar } from "@/components/common/ProgressBar";
import { Icon } from "@/components/common/Icon";

export function AdminDashboard() {
  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">ADMIN PANEL</p>
          <h1>Dashboard</h1>
          <p>System overview and management tools (Placeholder - Coming Soon)</p>
        </div>
      </div>

      <div className="stats-grid">
        <Card className="stat-card">
          <span className="stat-icon stat-indigo">
            <Icon name="user" />
          </span>
          <div>
            <strong>1,234</strong>
            <p>Total users</p>
          </div>
        </Card>
        <Card className="stat-card">
          <span className="stat-icon stat-green">
            <Icon name="chart" />
          </span>
          <div>
            <strong>56.8K</strong>
            <p>Words learned</p>
          </div>
        </Card>
        <Card className="stat-card">
          <span className="stat-icon stat-amber">
            <Icon name="star" />
          </span>
          <div>
            <strong>342</strong>
            <p>Premium subscriptions</p>
          </div>
        </Card>
        <Card className="stat-card">
          <span className="stat-icon stat-violet">
            <Icon name="spark" />
          </span>
          <div>
            <strong>9.2K</strong>
            <p>AI requests</p>
          </div>
        </Card>
      </div>

      <div className="admin-grid">
        <Card>
          <div className="section-title">
            <div>
              <h3>User activity</h3>
              <p>Last 7 days</p>
            </div>
          </div>
          <div className="activity-list">
            <div className="activity-item">
              <span>New signups</span>
              <strong>156</strong>
              <ProgressBar value={75} />
            </div>
            <div className="activity-item">
              <span>Active users</span>
              <strong>892</strong>
              <ProgressBar value={85} />
            </div>
            <div className="activity-item">
              <span>Premium trials</span>
              <strong>42</strong>
              <ProgressBar value={40} />
            </div>
          </div>
        </Card>

        <Card>
          <div className="section-title">
            <div>
              <h3>System health</h3>
              <p>Current status</p>
            </div>
            <Badge tone="success">All good</Badge>
          </div>
          <div className="health-list">
            <div className="health-item">
              <span>API uptime</span>
              <strong>99.9%</strong>
            </div>
            <div className="health-item">
              <span>Database</span>
              <strong>Healthy</strong>
            </div>
            <div className="health-item">
              <span>Cache</span>
              <strong>92% hit rate</strong>
            </div>
            <div className="health-item">
              <span>Queue</span>
              <strong>2.3s avg</strong>
            </div>
          </div>
        </Card>
      </div>

      <Card className="placeholder-card">
        <div className="placeholder-content">
          <span>
            <Icon name="settings" size="lg" />
          </span>
          <h2>Admin features coming soon</h2>
          <p>
            Full admin dashboard with user management, analytics, content moderation, and
            system settings will be available in the next phase.
          </p>
          <div className="placeholder-features">
            <Badge>User Management</Badge>
            <Badge>Content Moderation</Badge>
            <Badge>Analytics</Badge>
            <Badge>Settings</Badge>
          </div>
        </div>
      </Card>
    </div>
  );
}
