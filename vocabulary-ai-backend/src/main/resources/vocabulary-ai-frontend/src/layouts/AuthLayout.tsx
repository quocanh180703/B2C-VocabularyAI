import { ReactNode } from "react";
import { Icon } from "@/components/common/Icon";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { Badge } from "@/components/common/Badge";

interface AuthLayoutProps {
  children: ReactNode;
  theme: "light" | "dark";
  onToggleTheme: () => void;
}

export function AuthLayout({ children, theme, onToggleTheme }: AuthLayoutProps) {
  return (
    <main className="auth-page">
      <section className="auth-story">
        <div className="auth-story-inner">
          <div className="brand auth-brand">
            <span className="brand-mark">
              <Icon name="cards" />
            </span>
            <span>
              Vocabulary<span>AI</span>
            </span>
          </div>
          <div className="auth-story-copy">
            <Badge tone="primary">
              <Icon name="spark" size="sm" /> AI-POWERED ENGLISH LEARNING
            </Badge>
            <h1>Build the English skills that move you forward.</h1>
            <p>
              Learn useful vocabulary, practice with personalized AI guidance, and turn a few
              focused minutes each day into lasting progress.
            </p>
            <div className="auth-benefits">
              <span>
                <i>
                  <Icon name="check" size="sm" />
                </i>
                <strong>Smart review</strong>
                <small>Remember more with spaced repetition</small>
              </span>
              <span>
                <i>
                  <Icon name="spark" size="sm" />
                </i>
                <strong>Personal AI support</strong>
                <small>Clear explanations whenever you need them</small>
              </span>
              <span>
                <i>
                  <Icon name="chart" size="sm" />
                </i>
                <strong>Visible progress</strong>
                <small>Stay motivated with practical insights</small>
              </span>
            </div>
          </div>
          <p className="auth-quote">
            "Consistency turns unfamiliar words into confident communication."
          </p>
        </div>
      </section>
      <section className="auth-panel">
        <div className="auth-theme">
          <ThemeToggle theme={theme} toggle={onToggleTheme} />
        </div>
        <div className="auth-mobile-brand">
          <span className="brand-mark">
            <Icon name="cards" />
          </span>
          <strong>VocabularyAI</strong>
        </div>
        {children}
      </section>
    </main>
  );
}
