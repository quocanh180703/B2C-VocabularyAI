/**
 * Types and interfaces for VocabularyAI Frontend
 */

// Export Learning module types
export * from './learning';

// Export Quiz module types
export * from './quiz';

export type Page =
  | "dashboard"
  | "vocabulary"
  | "learn"
  | "quiz"
  | "ai"
  | "speaking"
  | "progress"
  | "premium"
  | "settings";

export type IconName =
  | "home"
  | "book"
  | "cards"
  | "quiz"
  | "spark"
  | "mic"
  | "chart"
  | "settings"
  | "search"
  | "bell"
  | "fire"
  | "target"
  | "clock"
  | "arrow"
  | "volume"
  | "star"
  | "check"
  | "close"
  | "brain"
  | "menu"
  | "chevron"
  | "send"
  | "sun"
  | "moon"
  | "user"
  | "mail"
  | "lock"
  | "eye";

export interface Word {
  word: string;
  phonetic: string;
  meaning: string;
  translation: string;
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  type: string;
  category: string;
}

export interface StatCard {
  icon: IconName;
  value: string;
  label: string;
  tone: "amber" | "indigo" | "green" | "violet";
}

export interface NavItem {
  id: Page;
  label: string;
  icon: IconName;
}

export interface Badge {
  children: React.ReactNode;
  tone?: "neutral" | "primary" | "success" | "warning" | "danger";
}

export interface ButtonProps {
  children: React.ReactNode;
  variant?: "primary" | "secondary" | "ghost" | "success" | "danger";
  icon?: IconName;
  onClick?: () => void;
  disabled?: boolean;
  type?: "button" | "submit";
  className?: string;
}

export interface QuizCard {
  id: number;
  title: string;
  tag: string;
  questions: number;
  time: string;
}

export interface QuizState {
  active: boolean;
  answer: number | null;
  result: boolean;
}

export interface SpeakingSession {
  id?: number;
  topic: string;
  level: string;
  description?: string;
}

export interface AIToolsTab {
  id: "explain" | "grammar" | "chat";
  label: string;
}

export interface GrammarError {
  text: string;
  suggestion: string;
  explanation: string;
}

export interface ConversationMessage {
  id?: number;
  role: "user" | "assistant";
  content: string;
  timestamp?: Date;
}

export interface ThemeName {
  theme: "light" | "dark";
  toggle: () => void;
}
