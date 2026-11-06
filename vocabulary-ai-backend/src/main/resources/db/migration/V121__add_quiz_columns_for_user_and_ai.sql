-- Migration V121: Add user ownership and AI metadata columns to quizzes table
ALTER TABLE quizzes
    ADD COLUMN created_by BIGINT NULL AFTER category_id,
    ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' AFTER status,
    ADD COLUMN is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE AFTER visibility,
    ADD COLUMN source_document_id BIGINT NULL AFTER is_ai_generated,
    ADD COLUMN review_note TEXT NULL AFTER source_document_id,
    ADD COLUMN reviewed_by BIGINT NULL AFTER review_note,
    ADD COLUMN reviewed_at DATETIME NULL AFTER reviewed_by;

CREATE INDEX idx_quizzes_created_by ON quizzes (created_by);
CREATE INDEX idx_quizzes_visibility ON quizzes (visibility);

ALTER TABLE quizzes
    ADD CONSTRAINT fk_quizzes_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_quizzes_source_document FOREIGN KEY (source_document_id) REFERENCES user_documents (id) ON DELETE SET NULL;
