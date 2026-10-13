/**
 * Personalization Tab Component
 * Displays personalization features and adaptive learning insights
 */

import {
  useProfile,
  useLearningPlan,
  useProgressAnalysis,
  useRecommendations,
  useWeaknessStrength,
  useAdaptiveDifficulty,
  useDailyPractice,
} from "@/hooks/usePersonalization";
import { SkeletonList } from "@/components/common";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";

interface PersonalizationTabProps {
  activeTab?: "overview" | "plan" | "progress" | "recommendations" | "analysis";
  onTabChange?: (tab: string) => void;
}

export default function PersonalizationTab({
  activeTab = "overview",
  onTabChange,
}: PersonalizationTabProps) {
  const profileQuery = useProfile();
  const learningPlanQuery = useLearningPlan({ weeks: 4 });
  const progressQuery = useProgressAnalysis({ daysBack: 30 });
  const recommendationsQuery = useRecommendations({ limit: 5 });
  const weaknessStrengthQuery = useWeaknessStrength();
  const adaptiveDifficultyQuery = useAdaptiveDifficulty();
  const dailyPracticeQuery = useDailyPractice();

  const isLoading =
    profileQuery.isLoading ||
    learningPlanQuery.isLoading ||
    progressQuery.isLoading ||
    recommendationsQuery.isLoading;

  const isError =
    profileQuery.isError ||
    learningPlanQuery.isError ||
    progressQuery.isError ||
    recommendationsQuery.isError;

  if (isLoading) {
    return <SkeletonList count={3} />;
  }

  if (isError) {
    return (
      <ErrorBoundary
        message="Failed to load personalization data"
        onRetry={() => {
          profileQuery.refetch();
          learningPlanQuery.refetch();
          progressQuery.refetch();
          recommendationsQuery.refetch();
        }}
      />
    );
  }

  const profile = profileQuery.data;
  const learningPlan = learningPlanQuery.data;
  const progress = progressQuery.data;
  const recommendations = recommendationsQuery.data;
  const weaknessStrength = weaknessStrengthQuery.data;
  const adaptiveDifficulty = adaptiveDifficultyQuery.data;
  const dailyPractice = dailyPracticeQuery.data;

  return (
    <ErrorBoundary>
      <div className="w-full space-y-6">
        {/* Header */}
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
          <h2 className="text-xl font-semibold text-slate-900 dark:text-white">
            Personalized Learning
          </h2>
          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
            Your adaptive learning journey tailored to your goals
          </p>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 border-b border-slate-200 dark:border-slate-700">
          {[
            { id: "overview", label: "Overview" },
            { id: "plan", label: "Learning Plan" },
            { id: "progress", label: "Progress" },
            { id: "recommendations", label: "Recommendations" },
            { id: "analysis", label: "Analysis" },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => onTabChange?.(tab.id)}
              className={`px-4 py-2 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? "border-b-2 border-indigo-600 text-indigo-600 dark:border-indigo-400 dark:text-indigo-400"
                  : "text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white"
              }`}
              aria-selected={activeTab === tab.id}
              role="tab"
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="space-y-6">
          {activeTab === "overview" && (
            <div className="space-y-4">
              {/* User Profile Overview */}
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                  <p className="text-sm font-medium text-slate-600 dark:text-slate-400">
                    Current Level
                  </p>
                  <p className="mt-2 text-2xl font-bold text-indigo-600 dark:text-indigo-400">
                    {profile?.proficiencyLevel}
                  </p>
                </div>

                <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                  <p className="text-sm font-medium text-slate-600 dark:text-slate-400">
                    Current Streak
                  </p>
                  <p className="mt-2 text-2xl font-bold text-amber-600 dark:text-amber-400">
                    {profile?.currentStreak} days
                  </p>
                </div>

                <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                  <p className="text-sm font-medium text-slate-600 dark:text-slate-400">
                    Total Learning
                  </p>
                  <p className="mt-2 text-2xl font-bold text-green-600 dark:text-green-400">
                    {profile?.totalLearningMinutes}m
                  </p>
                </div>

                <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                  <p className="text-sm font-medium text-slate-600 dark:text-slate-400">
                    Learning Style
                  </p>
                  <p className="mt-2 text-lg font-semibold text-violet-600 dark:text-violet-400">
                    {profile?.learningStyle}
                  </p>
                </div>
              </div>

              {/* Daily Goal */}
              <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                <h3 className="font-semibold text-slate-900 dark:text-white">
                  Today's Practice
                </h3>
                {dailyPractice ? (
                  <div className="mt-4 space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-600 dark:text-slate-400">
                        Progress: {dailyPractice.completedMinutes}/{dailyPractice.totalEstimatedMinutes} minutes
                      </span>
                      <span className="font-medium text-indigo-600 dark:text-indigo-400">
                        {Math.round((dailyPractice.completedMinutes / dailyPractice.totalEstimatedMinutes) * 100)}%
                      </span>
                    </div>
                    <div className="h-2 rounded-full bg-slate-200 dark:bg-slate-700">
                      <div
                        className="h-full rounded-full bg-indigo-600 transition-all dark:bg-indigo-400"
                        style={{
                          width: `${(dailyPractice.completedMinutes / dailyPractice.totalEstimatedMinutes) * 100}%`,
                        }}
                      />
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          )}

          {activeTab === "plan" && (
            <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-900 dark:text-white">
                Your Learning Plan
              </h3>
              {learningPlan ? (
                <div className="mt-4 space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-slate-600 dark:text-slate-400">
                      Overall Progress
                    </span>
                    <span className="font-medium text-indigo-600 dark:text-indigo-400">
                      {learningPlan.overallProgress}%
                    </span>
                  </div>
                  <div className="h-2 rounded-full bg-slate-200 dark:bg-slate-700">
                    <div
                      className="h-full rounded-full bg-indigo-600 transition-all dark:bg-indigo-400"
                      style={{ width: `${learningPlan.overallProgress}%` }}
                    />
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-400">
                    Focus Areas: {learningPlan.focusAreas.join(", ")}
                  </p>
                </div>
              ) : null}
            </div>
          )}

          {activeTab === "progress" && (
            <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-900 dark:text-white">
                Progress Analysis
              </h3>
              {progress ? (
                <div className="mt-4 space-y-4">
                  {progress.metrics.map((metric) => (
                    <div key={metric.name}>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-slate-600 dark:text-slate-400">
                          {metric.name}
                        </span>
                        <span className="font-medium text-slate-900 dark:text-white">
                          {metric.value}/{metric.target}
                        </span>
                      </div>
                      <div className="mt-1 h-2 rounded-full bg-slate-200 dark:bg-slate-700">
                        <div
                          className="h-full rounded-full bg-indigo-600 transition-all dark:bg-indigo-400"
                          style={{ width: `${metric.progress}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          )}

          {activeTab === "recommendations" && (
            <div className="space-y-3">
              {recommendations?.recommendations.map((rec) => (
                <div
                  key={rec.id}
                  className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-900"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="font-medium text-slate-900 dark:text-white">
                        {rec.title}
                      </h4>
                      <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
                        {rec.description}
                      </p>
                      <p className="mt-2 text-xs text-slate-500 dark:text-slate-500">
                        Reason: {rec.reason}
                      </p>
                    </div>
                    <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                      rec.priority === "high"
                        ? "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200"
                        : rec.priority === "medium"
                          ? "bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200"
                          : "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200"
                    }`}>
                      {rec.priority}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeTab === "analysis" && (
            <div className="space-y-6">
              {/* Strengths */}
              <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                <h3 className="font-semibold text-green-600 dark:text-green-400">
                  Your Strengths
                </h3>
                <div className="mt-4 space-y-3">
                  {weaknessStrength?.strengths.map((strength) => (
                    <div key={strength.skillName} className="flex items-center justify-between">
                      <span className="text-slate-900 dark:text-white">{strength.skillName}</span>
                      <span className="text-sm font-medium text-green-600 dark:text-green-400">
                        {strength.proficiency}%
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Weaknesses */}
              <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                <h3 className="font-semibold text-amber-600 dark:text-amber-400">
                  Areas to Improve
                </h3>
                <div className="mt-4 space-y-3">
                  {weaknessStrength?.weaknesses.map((weakness) => (
                    <div key={weakness.skillName} className="flex items-center justify-between">
                      <span className="text-slate-900 dark:text-white">{weakness.skillName}</span>
                      <span className="text-sm font-medium text-amber-600 dark:text-amber-400">
                        {weakness.proficiency}%
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Current Difficulty Level */}
              {adaptiveDifficulty && (
                <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
                  <h3 className="font-semibold text-slate-900 dark:text-white">
                    Adaptive Difficulty
                  </h3>
                  <div className="mt-4 space-y-2">
                    <p className="text-sm text-slate-600 dark:text-slate-400">
                      Current Level: <span className="font-medium">{adaptiveDifficulty.currentLevel}</span>
                    </p>
                    <p className="text-sm text-slate-600 dark:text-slate-400">
                      Next Level: <span className="font-medium">{adaptiveDifficulty.nextLevel}</span>
                    </p>
                    <div className="mt-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700">
                      <div
                        className="h-full rounded-full bg-indigo-600 transition-all dark:bg-indigo-400"
                        style={{ width: `${adaptiveDifficulty.progressToNextLevel}%` }}
                      />
                    </div>
                    <p className="text-xs text-slate-500 dark:text-slate-500">
                      {adaptiveDifficulty.progressToNextLevel}% to next level
                    </p>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </ErrorBoundary>
  );
}
