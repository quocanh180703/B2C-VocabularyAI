import { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useParams } from "react-router-dom";
import { Page, NavItem } from "@/types";
import { useTheme } from "@/hooks/useTheme";
import { useAuth } from "@/hooks/useAuth";

// Components
import { ProtectedRoute } from "@/components/common/ProtectedRoute";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";

// Layouts
import { MainLayout, AuthLayout } from "@/layouts";

// Auth Pages
import { LoginPage, RegisterPage, ForgotPasswordPage } from "@/pages/auth";

// App Pages
import { DashboardPage } from "@/pages/dashboard";
import { VocabularyPage, WordDetailPage } from "@/pages/vocabulary";
import LearningPage from "@/pages/Learning";
import ReviewQueuePage from "@/pages/ReviewQueue";
import LearningHistoryPage from "@/pages/LearningHistory";
import { QuizPage } from "@/pages/quiz";
import { AIToolsPage } from "@/pages/ai";
import { SpeakingPage } from "@/pages/speaking";
import { PricingPage, PaymentHistoryPage, PaymentSuccessPage, PaymentCancelPage } from "@/pages/payment";
import { AdminDashboard } from "@/pages/admin";
import { ProfilePage } from "@/pages/profile/ProfilePage";

// Wrapper for WordDetailPage to extract route params
function WordDetailPageWrapper() {
  const { id } = useParams<{ id: string }>();
  return <WordDetailPage wordId={id || ''} onBack={() => window.history.back()} />;
}

// Placeholder pages
const ProgressPage = () => (
  <div className="page">
    <div className="page-heading">
      <div>
        <p className="eyebrow">YOUR LEARNING JOURNEY</p>
        <h1>My progress</h1>
        <p>Progress page - coming soon</p>
      </div>
    </div>
  </div>
);

const SettingsPage = () => (
  <div className="page">
    <div className="page-heading">
      <div>
        <p className="eyebrow">ACCOUNT & PREFERENCES</p>
        <h1>Settings</h1>
        <p>Settings page - coming soon</p>
      </div>
    </div>
  </div>
);

export default function App() {
  const { theme, toggleTheme } = useTheme();
  const { isAuthenticated } = useAuth();

  const navItems: NavItem[] = [
    { id: "dashboard", label: "Dashboard", icon: "home" },
    { id: "vocabulary", label: "Vocabulary", icon: "book" },
    { id: "learn", label: "Flashcards", icon: "cards" },
    { id: "quiz", label: "Quiz", icon: "quiz" },
    { id: "ai", label: "AI Tools", icon: "spark" },
    { id: "speaking", label: "Speaking", icon: "mic" },
    { id: "progress", label: "Progress", icon: "chart" },
    { id: "premium", label: "Premium", icon: "star" },
    { id: "settings", label: "Settings", icon: "settings" },
  ];

  // Set theme attribute on document
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  return (
    <Router>
      <Routes>
        {/* Auth Routes */}
        <Route
          path="/login"
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <AuthLayout theme={theme} onToggleTheme={toggleTheme}>
                <LoginPage />
              </AuthLayout>
            )
          }
        />
        <Route
          path="/register"
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <AuthLayout theme={theme} onToggleTheme={toggleTheme}>
                <RegisterPage />
              </AuthLayout>
            )
          }
        />
        <Route
          path="/forgot-password"
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <AuthLayout theme={theme} onToggleTheme={toggleTheme}>
                <ForgotPasswordPage />
              </AuthLayout>
            )
          }
        />

        {/* Main App Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="dashboard"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <DashboardPage onNavigate={() => {}} />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="dashboard"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <DashboardPage onNavigate={() => {}} />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/vocabulary"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="vocabulary"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <VocabularyPage onOpenDetail={() => {}} />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/vocabulary/:id"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="vocabulary"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <WordDetailPageWrapper />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/learn"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="learn"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <LearningPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/quiz"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="quiz"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <QuizPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/ai"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="ai"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <AIToolsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/speaking"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="speaking"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <SpeakingPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/premium"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="premium"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <PricingPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/progress"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="progress"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <ProgressPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="settings"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <SettingsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        {/* Admin Routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="admin">
              <MainLayout
                currentPage="dashboard"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <AdminDashboard />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="dashboard"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <ProfilePage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Payment Routes */}
        <Route
          path="/pricing"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="premium"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <PricingPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/payments/history"
          element={
            <ProtectedRoute>
              <MainLayout
                currentPage="premium"
                onNavigate={() => {}}
                navItems={navItems}
                theme={theme}
                onToggleTheme={toggleTheme}
              >
                <PaymentHistoryPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/payment/success"
          element={
            <MainLayout
              currentPage="dashboard"
              onNavigate={() => {}}
              navItems={navItems}
              theme={theme}
              onToggleTheme={toggleTheme}
            >
              <PaymentSuccessPage />
            </MainLayout>
          }
        />
        <Route
          path="/payment/cancel"
          element={
            <MainLayout
              currentPage="dashboard"
              onNavigate={() => {}}
              navItems={navItems}
              theme={theme}
              onToggleTheme={toggleTheme}
            >
              <PaymentCancelPage />
            </MainLayout>
          }
        />

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} />
      </Routes>
    </Router>
  );
}
