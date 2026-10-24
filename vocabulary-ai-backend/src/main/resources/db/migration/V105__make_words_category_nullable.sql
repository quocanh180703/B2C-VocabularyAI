-- Migration to make category_id in words table nullable
-- Required because CEFR vocabulary words are added dynamically without requiring a fixed category
ALTER TABLE words MODIFY COLUMN category_id BIGINT NULL;
