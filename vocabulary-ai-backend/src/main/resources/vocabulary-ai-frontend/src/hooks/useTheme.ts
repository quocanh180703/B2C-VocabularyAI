import { useEffect, useState } from "react";

/**
 * Hook for managing light/dark theme
 */
export const useTheme = () => {
  const [theme, setTheme] = useState<"light" | "dark">("light");

  const toggleTheme = () => {
    setTheme((prev) => (prev === "light" ? "dark" : "light"));
  };

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  return { theme, toggleTheme };
};
