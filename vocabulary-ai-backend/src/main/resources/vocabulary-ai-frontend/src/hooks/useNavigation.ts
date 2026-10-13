import { useState } from "react";
import { Page } from "@/types";

/**
 * Hook for managing page navigation and detail views
 */
export const useNavigation = () => {
  const [page, setPage] = useState<Page>("dashboard");
  const [detailWordId, setDetailWordId] = useState<string | null>(null);

  const go = (newPage: Page) => {
    setPage(newPage);
    setDetailWordId(null);
  };

  const openDetail = () => {
    setDetailWordId("abandon"); // Replace with dynamic ID if needed
  };

  const closeDetail = () => {
    setDetailWordId(null);
  };

  return {
    page,
    go,
    detailWordId,
    openDetail,
    closeDetail,
  };
};
