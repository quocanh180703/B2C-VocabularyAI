/**
 * Personalization Module Types
 * Type definitions for personalization/adaptive learning APIs
 */

// ============================================
// USER PROFILE TYPES
// ============================================

export interface UserProfile {
  userId: number;
  username: string;
  email: string;
  nativeLanguage: string;
  targetLanguage: string;
  proficiencyLevel: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  learningStyle: "visual" | "auditory" | "kinesthetic" | "reading-writing";
  dailyGoal: number; // minutes
  joinedAt: string;
  lastActive: string;
  totalLearningMinutes: number;
  currentStreak: number;
  longestStreak: number;
}

// ============================================
// LEARNING PLAN TYPES
// ============================================

export interface LearningPlanItem {
  day: number;
  date: string;
  topic: string;
  estimatedMinutes: number;
  priority: "high" | "medium" | "low";
  completed: boolean;
  activities: LearningActivity[];
}

export interface LearningActivity {
  id: number;
  type: "vocabulary" | "grammar" | "listening" | "speaking" | "reading" | "writing";
  title: string;
  description: string;
  estimatedMinutes: number;
  completed: boolean;
  completedAt?: string;
}

export interface LearningPlan {
  planId: number;
  userId: number;
  startDate: string;
  endDate: string;
  totalDays: number;
  completedDays: number;
  weeklyHours: number;
  focusAreas: string[];
  items: LearningPlanItem[];
  overallProgress: number; // 0-100
}

// ============================================
// PROGRESS ANALYSIS TYPES
// ============================================

export interface ProgressMetric {
  name: string;
  value: number;
  target: number;
  progress: number; // percentage
  trend: "up" | "down" | "stable";
}

export interface WeeklyProgress {
  week: number;
  startDate: string;
  endDate: string;
  minutesSpent: number;
  sessionsCompleted: number;
  wordsLearned: number;
  accuracy: number;
  averageScore: number;
}

export interface ProgressAnalysis {
  userId: number;
  analysisDate: string;
  overallProgress: number;
  weeklyProgress: WeeklyProgress[];
  metrics: ProgressMetric[];
  estimatedNextLevelDate: string;
  estimatedCompletionDate: string;
}

// ============================================
// RECOMMENDATIONS TYPES
// ============================================

export interface Recommendation {
  id: number;
  type: "vocabulary" | "grammar" | "practice" | "review" | "challenge";
  title: string;
  description: string;
  reason: string; // why it's recommended
  difficulty: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  priority: "high" | "medium" | "low";
  content: RecommendationContent;
  estimatedMinutes: number;
}

export interface RecommendationContent {
  words?: string[];
  topics?: string[];
  grammarRules?: string[];
  practiceQuestions?: number;
}

export interface RecommendationsResponse {
  userId: number;
  generatedAt: string;
  recommendations: Recommendation[];
  nextReviewDate: string;
}

// ============================================
// WEAKNESS/STRENGTH TYPES
// ============================================

export interface SkillAnalysis {
  skillName: string;
  proficiency: number; // 0-100
  level: "beginner" | "intermediate" | "advanced" | "expert";
  accuracy: number; // percentage
  averageResponseTime: number; // milliseconds
  attemptCount: number;
  successCount: number;
  lastPracticed: string;
  recommendedAction: string;
}

export interface WeaknessStrengthAnalysis {
  userId: number;
  analysisDate: string;
  overallProficiency: number;
  strengths: SkillAnalysis[];
  weaknesses: SkillAnalysis[];
  topicBreakdown: {
    topic: string;
    proficiency: number;
    trend: "improving" | "declining" | "stable";
  }[];
  recommendations: string[];
}

// ============================================
// ADAPTIVE DIFFICULTY TYPES
// ============================================

export interface DifficultyLevel {
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  name: string;
  description: string;
  recommendedMinutes: number;
  contentCount: number;
  difficulty: number; // 0-100
}

export interface AdaptiveDifficulty {
  userId: number;
  currentLevel: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  nextLevel: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  progressToNextLevel: number; // percentage
  adjustmentRecommended: boolean;
  availableLevels: DifficultyLevel[];
  performanceHistory: {
    date: string;
    level: string;
    accuracy: number;
    averageScore: number;
  }[];
}

// ============================================
// DAILY PRACTICE TYPES
// ============================================

export interface DailyPracticeItem {
  id: number;
  type: "vocabulary" | "grammar" | "listening" | "speaking" | "reading" | "writing";
  title: string;
  description: string;
  estimatedMinutes: number;
  difficulty: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  content: string;
  completed: boolean;
  score?: number;
  feedback?: string;
}

export interface DailyPractice {
  date: string;
  totalEstimatedMinutes: number;
  completedMinutes: number;
  items: DailyPracticeItem[];
  streakDays: number;
  motivation: string;
  nextChallenge?: string;
}

// ============================================
// REQUEST/RESPONSE TYPES
// ============================================

export interface GetProfileRequest {
  userId?: number;
}

export interface GetLearningPlanRequest {
  userId?: number;
  weeks?: number;
}

export interface GetProgressAnalysisRequest {
  userId?: number;
  daysBack?: number;
}

export interface GetRecommendationsRequest {
  userId?: number;
  limit?: number;
}

export interface GetWeaknessStrengthRequest {
  userId?: number;
}

export interface GetAdaptiveDifficultyRequest {
  userId?: number;
}

export interface GetDailyPracticeRequest {
  userId?: number;
  date?: string;
}
