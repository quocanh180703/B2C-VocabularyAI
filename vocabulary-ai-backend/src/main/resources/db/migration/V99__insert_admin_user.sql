-- Admin user for testing
-- Email: admin@vocabularyai.vn
-- Username: admin
-- Password: admin123 (BCrypt hash)

INSERT IGNORE INTO users (username, email, password, full_name, role, status, created_at, updated_at)
VALUES (
    'admin',
    'admin@vocabularyai.vn',
    '$2a$10$xI.YRn/eNqVNFgVqHTkfAOlR.SvkNMb4vPRCu9F3jQ.nCKQVUKu5K',
    'Admin User',
    'ADMIN',
    'ACTIVE',
    NOW(),
    NOW()
);
