-- V115: Sync vietnamese_meaning from cefr_words to words table
-- Ensures words favorited earlier have valid Vietnamese translations instead of English definitions.

UPDATE words w
JOIN cefr_words c ON LOWER(w.word) = LOWER(c.lemma)
SET w.meaning = c.vietnamese_meaning
WHERE c.vietnamese_meaning IS NOT NULL 
  AND c.vietnamese_meaning != ''
  AND (w.meaning IS NULL OR w.meaning = '' OR w.meaning = w.definition);

