type TabId = "explain" | "grammar" | "chat";

interface TabBarProps {
  activeTab: TabId;
  onTabChange: (tab: TabId) => void;
}

export function TabBar({ activeTab, onTabChange }: TabBarProps) {
  const tabs: Array<{ id: TabId; label: string }> = [
    { id: "explain", label: "Vocabulary explanation" },
    { id: "grammar", label: "Grammar check" },
    { id: "chat", label: "AI tutor" },
  ];

  return (
    <div className="tab-list">
      {tabs.map(({ id, label }) => (
        <button
          key={id}
          className={activeTab === id ? "active" : ""}
          onClick={() => onTabChange(id)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
