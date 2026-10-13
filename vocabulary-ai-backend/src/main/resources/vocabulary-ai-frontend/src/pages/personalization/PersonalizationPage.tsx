/**
 * Personalization Page
 * Full-page view for personalization and adaptive learning
 */

import { useState } from "react";
import PersonalizationTab from "@/components/ai/PersonalizationTab";

type PersonalizationTab = "overview" | "plan" | "progress" | "recommendations" | "analysis";

export default function PersonalizationPage() {
  const [activeTab, setActiveTab] = useState<PersonalizationTab>("overview");

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
            Personalized Learning
          </h1>
          <p className="mt-2 text-slate-600 dark:text-slate-400">
            Your adaptive learning journey tailored to your goals and learning style
          </p>
        </div>

        {/* Main Content */}
        <PersonalizationTab activeTab={activeTab} onTabChange={(tab) => setActiveTab(tab as PersonalizationTab)} />
      </div>
    </div>
  );
}
