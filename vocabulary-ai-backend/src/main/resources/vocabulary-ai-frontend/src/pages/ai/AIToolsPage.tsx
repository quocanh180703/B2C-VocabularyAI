import { useState } from "react";
import {
  ExplanationTab,
  GrammarCheckTab,
  ChatTab,
  TabBar,
} from "@/components/ai";

type TabId = "explain" | "grammar" | "chat";

export function AIToolsPage() {
  const [activeTab, setActiveTab] = useState<TabId>("explain");

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">POWERED BY GEMINI</p>
          <h1>AI learning tools</h1>
          <p>Get helpful explanations and feedback in your learning context.</p>
        </div>
      </div>
      <TabBar activeTab={activeTab} onTabChange={setActiveTab} />
      
      {/* Tab content - All connected to backend APIs */}
      {activeTab === "explain" && <ExplanationTab />}
      {activeTab === "grammar" && <GrammarCheckTab />}
      {activeTab === "chat" && <ChatTab />}
    </div>
  );
}
