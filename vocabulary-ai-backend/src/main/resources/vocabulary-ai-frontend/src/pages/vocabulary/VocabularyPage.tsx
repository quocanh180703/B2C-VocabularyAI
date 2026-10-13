import { useState, useEffect, useRef, useCallback } from "react";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { VocabCard, FilterCard, EmptyState } from "@/components/vocabulary";
import { useVocabulary } from "@/hooks/useVocabulary";
import { ErrorState } from "@/components/common";
import { VocabularySkeleton } from "@/components/vocabulary";

interface VocabularyPageProps {
  onOpenDetail: (wordId: string) => void;
}

/**
 * VocabularyPage Component
 * 
 * Features:
 * - Search with 300-500ms debounce (prevents request spam)
 * - CEFR level filtering (A1-C1)
 * - Pagination support
 * - Favorite management (if authenticated)
 * - Learning status tracking (if authenticated)
 * - Loading/error/empty states
 */
export function VocabularyPage({ onOpenDetail }: VocabularyPageProps) {
  const {
    words,
    totalElements,
    totalPages,
    currentPage,
    loading,
    error,
    search,
    refetch,
    isFavorited,
    addToFavorites,
    removeFromFavorites,
    getLearningStatus,
  } = useVocabulary();

  const [query, setQuery] = useState("");
  const [level, setLevel] = useState("All");
  const [currentPageNum, setCurrentPageNum] = useState(0);
  const pageSize = 20;

  // Debounce timer ref
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);

  /**
   * Perform search with 300-500ms debounce
   * Prevents excessive API calls while user is typing
   */
  const performSearch = useCallback(async (searchQuery: string, searchLevel: string, page: number) => {
    console.log('🔍 Performing vocabulary search:', { searchQuery, searchLevel, page });
    
    try {
      await search(searchQuery, searchLevel, page, pageSize);
    } catch (err) {
      console.error('Search failed:', err);
    }
  }, [search, pageSize]);

  /**
   * Handle query change with debounce (300-500ms)
   */
  const handleQueryChange = useCallback((newQuery: string) => {
    setQuery(newQuery);
    setCurrentPageNum(0); // Reset to first page on new search

    // Clear previous debounce timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Set new debounce timer (400ms - middle of 300-500ms range)
    debounceTimerRef.current = setTimeout(() => {
      performSearch(newQuery, level, 0);
    }, 400);
  }, [level, performSearch]);

  /**
   * Handle level filter change (no debounce needed)
   */
  const handleLevelChange = useCallback((newLevel: string) => {
    setLevel(newLevel);
    setCurrentPageNum(0); // Reset to first page

    // Cancel any pending debounce search
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Perform immediate search (user explicitly changed filter)
    performSearch(query, newLevel, 0);
  }, [query, performSearch]);

  /**
   * Handle pagination
   */
  const handlePageChange = useCallback((newPage: number) => {
    setCurrentPageNum(newPage);

    // Cancel any pending debounce search
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Perform immediate search for pagination
    performSearch(query, level, newPage);
  }, [query, level, performSearch]);

  /**
   * Handle clear filters
   */
  const handleClearFilters = useCallback(() => {
    setQuery("");
    setLevel("All");
    setCurrentPageNum(0);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    performSearch("", "All", 0);
  }, [performSearch]);

  /**
   * Initial search on mount
   */
  useEffect(() => {
    performSearch("", "All", 0);

    // Cleanup debounce timer on unmount
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [performSearch]);

  /**
   * Handle favorite toggle
   */
  const handleToggleFavorite = useCallback(async (wordId: number, isFav: boolean) => {
    try {
      if (isFav) {
        await removeFromFavorites(wordId);
      } else {
        await addToFavorites(wordId);
      }
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    }
  }, [addToFavorites, removeFromFavorites]);

  // Render loading state
  if (loading && words.length === 0) {
    return <VocabularySkeleton />;
  }

  // Render error state
  if (error) {
    return (
      <ErrorState 
        title="Failed to load vocabulary"
        message={error.message}
        onRetry={refetch}
      />
    );
  }

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">EXPLORE & DISCOVER</p>
          <h1>Vocabulary library</h1>
          <p>Build a stronger English vocabulary, one useful word at a time.</p>
        </div>
        <Button icon="star" variant="secondary">
          My favorites
        </Button>
      </div>

      <FilterCard
        query={query}
        onQueryChange={handleQueryChange}
        level={level}
        onLevelChange={handleLevelChange}
      />

      <div className="results-heading">
        <p>
          <strong>{totalElements}</strong> words found
        </p>
        <button className="sort-control">
          Sort by: <strong>Recommended</strong> <Icon name="chevron" size="sm" />
        </button>
      </div>

      {words.length > 0 ? (
        <>
          <div className="vocab-grid">
            {words.map((word) => {
              const wordId = parseInt(word.id) || 0;
              const isFav = isFavorited(wordId);
              const learningStatus = getLearningStatus(wordId);

              return (
                <VocabCard
                  key={word.id}
                  word={word.lemma}
                  phonetic={word.pronunciation}
                  meaning={word.definition}
                  level={word.cefrLevel}
                  type={word.partOfSpeech}
                  category={word.source}
                  isFavorite={isFav}
                  learningStatus={learningStatus?.status}
                  onViewWord={() => onOpenDetail(word.id)}
                  onToggleFavorite={() => handleToggleFavorite(wordId, isFav)}
                />
              );
            })}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              {currentPageNum > 0 && (
                <Button
                  variant="ghost"
                  onClick={() => handlePageChange(currentPageNum - 1)}
                >
                  ← Previous
                </Button>
              )}
              <span className="page-indicator">
                Page {currentPageNum + 1} of {totalPages}
              </span>
              {currentPageNum < totalPages - 1 && (
                <Button
                  variant="ghost"
                  onClick={() => handlePageChange(currentPageNum + 1)}
                >
                  Next →
                </Button>
              )}
            </div>
          )}
        </>
      ) : (
        <EmptyState onClearFilters={handleClearFilters} />
      )}
    </div>
  );
}
