import { Page, Word } from "@/types";
import { Icon } from "@/components/common/Icon";
import { EmptyState } from "@/components/common/EmptyState";
import { ErrorState } from "@/components/common/ErrorState";
import { StatsCard, ReviewCard, GoalCard, PlanStatusCard, RecentWordsCard, InsightCard, DashboardSkeleton } from "@/components/dashboard";
import { useDashboard } from "@/hooks/useDashboard";

interface DashboardPageProps {
  onNavigate: (page: Page) => void;
}

export function DashboardPage({ onNavigate }: DashboardPageProps) {
  const { data, loading, error, refetch } = useDashboard();

  console.log('Dashboard state:', { data, loading, error }); // DEBUG

  // Get today's date
  const today = new Date();
  const dateFormatter = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
  const todayString = dateFormatter.format(today);

  // Loading state
  if (loading) {
    return <DashboardSkeleton />;
  }

  // Error state
  if (error || !data || !data.statistics) {
    console.error('Dashboard error:', { error, data }); // DEBUG
    return (
      <ErrorState
        title="Unable to load dashboard"
        error={error || undefined}
        onRetry={refetch}
        onContact={() => {
          // Contact support action
          console.log("Contact support");
        }}
      />
    );
  }

  const { user, statistics, reviewQueue, favoriteWords, recommendations } = data;

  console.log('Dashboard data:', { user, statistics, reviewQueue, favoriteWords, recommendations }); // DEBUG

  // Calculate words mastered (accuracy >= 80%)
  const wordsMastered = Math.round(
    (statistics?.totalWordsLearned ?? 0) * ((statistics?.averageAccuracy ?? 0) / 100)
  );

  // Build stats cards with real data
  const stats = [
    {
      icon: "fire" as const,
      value: `${statistics?.currentStreak ?? 0} day${(statistics?.currentStreak ?? 0) !== 1 ? "s" : ""}`,
      label: "Current streak",
      tone: "amber" as const,
    },
    {
      icon: "book" as const,
      value: (statistics?.totalWordsLearned ?? 0).toString(),
      label: "Words learned",
      tone: "indigo" as const,
    },
    {
      icon: "target" as const,
      value: wordsMastered.toString(),
      label: "Words mastered",
      tone: "green" as const,
    },
    {
      icon: "clock" as const,
      value: (reviewQueue?.totalElements ?? 0).toString(),
      label: "Reviews due",
      tone: "violet" as const,
    },
  ];

  // Build goal items with real data
  const goalItems = [
    {
      icon: "check" as const,
      title: "Learn new words",
      description: `${statistics?.dailyGoalProgress ?? 0} of ${statistics?.dailyGoalTarget ?? 0} completed`,
      badge:
        (statistics?.dailyGoalProgress ?? 0) === (statistics?.dailyGoalTarget ?? 0)
          ? { tone: "success" as const, label: "Done" }
          : {
              tone: "warning" as const,
              label: `${(statistics?.dailyGoalTarget ?? 0) - (statistics?.dailyGoalProgress ?? 0)} left`,
            },
    },
    {
      icon: "cards" as const,
      title: "Daily review",
      description: `${reviewQueue?.totalElements ?? 0} due for review`,
      badge:
        (reviewQueue?.totalElements ?? 0) > 0
          ? { tone: "warning" as const, label: `${reviewQueue?.totalElements ?? 0} due` }
          : { tone: "success" as const, label: "Complete" },
      isActive: (reviewQueue?.totalElements ?? 0) > 0,
    },
    {
      icon: "quiz" as const,
      title: "Quick quiz",
      description: recommendations?.title || "Recommended for you",
    },
  ];

  // Convert favorite words to Word type
  const recentWordsData: Word[] = (favoriteWords?.content ?? []).map((fw) => ({
    word: fw.word,
    phonetic: "", // Not provided by API
    meaning: fw.definition,
    translation: fw.definition, // Use definition as fallback
    level: fw.level,
    type: "Noun", // Default type, should be fetched from word details if needed
    category: fw.category,
  }));

  // Calculate goal progress percentage
  const goalProgress = Math.round(((statistics?.dailyGoalProgress ?? 0) / (statistics?.dailyGoalTarget ?? 1)) * 100);

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">{todayString}</p>
          <h1>Good morning, {user?.fullName?.split(" ")[0] ?? "User"}</h1>
          <p>Ready to make a little more progress today?</p>
        </div>
        <div className="streak-pill">
          <Icon name="fire" />
          <strong>{statistics?.currentStreak ?? 0}</strong>
          <span>day streak</span>
        </div>
      </div>

      <div className="stats-grid">
        {stats.map((stat) => (
          <StatsCard
            key={stat.label}
            icon={stat.icon}
            value={stat.value}
            label={stat.label}
            tone={stat.tone}
          />
        ))}
      </div>

      <PlanStatusCard onUpgrade={() => onNavigate("premium")} />

      <div className="dashboard-grid">
        {reviewQueue.totalElements > 0 ? (
          <ReviewCard onStartReview={() => onNavigate("learn")} />
        ) : (
          <div style={{ padding: "20px", backgroundColor: "#f9fafb", borderRadius: "8px", border: "1px solid #e5e7eb" }}>
            <EmptyState
              icon="check"
              title="All caught up!"
              description="No words need review today. Great job keeping up with your learning!"
              action={{
                label: "Learn new words",
                onClick: () => onNavigate("vocabulary")
              }}
            />
          </div>
        )}
        <GoalCard progress={goalProgress} completed={statistics.dailyGoalProgress} total={statistics.dailyGoalTarget} goals={goalItems} />
      </div>

      <div className="lower-grid">
        {recentWordsData.length > 0 ? (
          <RecentWordsCard
            words={recentWordsData}
            onViewAll={() => onNavigate("vocabulary")}
          />
        ) : (
          <div style={{ padding: "20px", backgroundColor: "#f9fafb", borderRadius: "8px", border: "1px solid #e5e7eb" }}>
            <EmptyState
              icon="book"
              title="No favorite words yet"
              description="Start learning and mark words as favorites to see them here."
              action={{
                label: "Start learning",
                onClick: () => onNavigate("vocabulary")
              }}
            />
          </div>
        )}
        {recommendations.description && recommendations.tags && recommendations.tags.length > 0 ? (
          <InsightCard
            quote={recommendations.description}
            tags={recommendations.tags}
            onReview={() => onNavigate("learn")}
          />
        ) : (
          <div style={{ padding: "20px", backgroundColor: "#f9fafb", borderRadius: "8px", border: "1px solid #e5e7eb" }}>
            <EmptyState
              icon="spark"
              title="No insights yet"
              description="Learn more words to get personalized AI insights about your learning."
              action={{
                label: "Continue learning",
                onClick: () => onNavigate("learn")
              }}
            />
          </div>
        )}
      </div>
    </div>
  );
}
