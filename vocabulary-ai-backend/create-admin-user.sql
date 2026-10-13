-- Create admin user for testing
-- Email: admin@vocabularyai.vn
-- Username: admin
-- Password: admin123
-- Role: ADMIN

-- First check if user exists
SELECT * FROM users WHERE email = 'admin@vocabularyai.vn' OR username = 'admin';

-- Insert admin user (if needed, adjust password hash as required)
-- Using BCrypt hash for password: admin123
INSERT INTO users (username, email, password, full_name, role, status, created_at, updated_at)
VALUES (
    'admin',
    'admin@vocabularyai.vn',
    '$2a$10$xI.YRn/eNqVNFgVqHTkfAOlR.SvkNMb4vPRCu9F3jQ.nCKQVUKu5K',
    'Admin User',
    'ADMIN',
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE 
    role = 'ADMIN',
    full_name = 'Admin User',
    status = 'ACTIVE',
    updated_at = NOW();

-- Verify admin user was created/updated
SELECT id, username, email, role, status FROM users WHERE email = 'admin@vocabularyai.vn';
