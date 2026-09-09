-- ============================================================
-- V10__insert_default_admin.sql
-- Thêm tài khoản Admin mặc định và dữ liệu test
-- ============================================================

-- ============================================================
-- 1. THÊM ADMIN USER
-- ============================================================
-- Tài khoản: admin
-- Mật khẩu: Admin@123 (đã hash bằng BCrypt)
-- Email: admin@vocabularyai.com
-- ============================================================

INSERT IGNORE INTO users (username, email, password, full_name, role, status, created_at, updated_at)
VALUES (
  'admin',
  'admin@vocabularyai.com',
  -- Password: Admin@123 (BCrypt hash - cần thay thế bằng real hash)
  '$2a$10$MgkEqZ7EqnvHU7apvIHCWOgdjC8Y7xNRHQCHG8JTOZsgKY9Hr3oRm',
  'Administrator',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
);

-- Lấy user_id của admin
SET @admin_user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);

-- ============================================================
-- 2. THÊM CATEGORIES (Danh mục từ vựng)
-- ============================================================

INSERT IGNORE INTO categories (name, description, created_at, updated_at)
VALUES 
  ('Common', 'Từ vựng thông dụng hàng ngày', NOW(), NOW()),
  ('Business', 'Từ vựng kinh doanh', NOW(), NOW()),
  ('Technology', 'Từ vựng công nghệ', NOW(), NOW()),
  ('Academic', 'Từ vựng học thuật', NOW(), NOW()),
  ('Travel', 'Từ vựng du lịch', NOW(), NOW());

-- Lấy category IDs
SET @cat_common = (SELECT id FROM categories WHERE name = 'Common' LIMIT 1);
SET @cat_business = (SELECT id FROM categories WHERE name = 'Business' LIMIT 1);
SET @cat_tech = (SELECT id FROM categories WHERE name = 'Technology' LIMIT 1);

-- ============================================================
-- 3. THÊM SAMPLE WORDS (Từ vựng mẫu)
-- ============================================================

INSERT IGNORE INTO words (category_id, word, pronunciation, part_of_speech, definition, example, level, created_at, updated_at)
VALUES
  (@cat_common, 'abandon', 'əˈbændən', 'verb', 'bỏ lại, từ bỏ', 'They abandoned the old house.', 'A2', NOW(), NOW()),
  (@cat_common, 'ability', 'əˈbɪlɪti', 'noun', 'khả năng', 'She has the ability to speak three languages.', 'A2', NOW(), NOW()),
  (@cat_common, 'abolish', 'əˈbɒlɪʃ', 'verb', 'bãi bỏ, hủy bỏ', 'The government abolished the law.', 'B1', NOW(), NOW()),
  (@cat_common, 'absolute', 'ˈæbsəluːt', 'adjective', 'tuyệt đối, hoàn toàn', 'This is an absolute disaster.', 'B1', NOW(), NOW()),
  (@cat_common, 'abstract', 'ˈæbstrækt', 'adjective', 'trừu tượng', 'Abstract art can be hard to understand.', 'B1', NOW(), NOW()),
  (@cat_business, 'account', 'əˈkaʊnt', 'noun', 'tài khoản, hóa đơn', 'Open a business account.', 'A2', NOW(), NOW()),
  (@cat_business, 'achieve', 'əˈtʃiːv', 'verb', 'đạt được, hoàn thành', 'We achieved our sales target.', 'A2', NOW(), NOW()),
  (@cat_business, 'acquire', 'əˈkwaɪə(r)', 'verb', 'mua, chiếm đoạt', 'The company acquired a new subsidiary.', 'B2', NOW(), NOW()),
  (@cat_tech, 'algorithm', 'ˈælɡərɪðəm', 'noun', 'thuật toán', 'Machine learning relies on complex algorithms.', 'B2', NOW(), NOW()),
  (@cat_tech, 'application', 'ˌæplɪˈkeɪʃən', 'noun', 'ứng dụng, ứng dụng phần mềm', 'Download the mobile application.', 'A2', NOW(), NOW());

-- ============================================================
-- 4. THÊM LEARNING PROGRESS (Tiến độ học tập)
-- ============================================================

-- Lấy một số word IDs từ bảng words
SET @word1 = (SELECT id FROM words WHERE word = 'abandon' LIMIT 1);
SET @word2 = (SELECT id FROM words WHERE word = 'ability' LIMIT 1);
SET @word3 = (SELECT id FROM words WHERE word = 'account' LIMIT 1);
SET @word4 = (SELECT id FROM words WHERE word = 'algorithm' LIMIT 1);

-- Thêm learning progress cho admin user
INSERT IGNORE INTO learning_progress (user_id, word_id, status, correct_count, wrong_count, review_count, last_reviewed_at, next_review_at, created_at, updated_at)
VALUES
  (@admin_user_id, @word1, 'MASTERED', 5, 0, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW()),
  (@admin_user_id, @word2, 'LEARNING', 3, 1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
  (@admin_user_id, @word3, 'LEARNING', 2, 2, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), NOW(), NOW()),
  (@admin_user_id, @word4, 'NEW', 0, 0, 0, NULL, NOW(), NOW(), NOW());

-- ============================================================
-- 5. THÊM LEARNING HISTORY (Lịch sử học tập)
-- ============================================================

INSERT IGNORE INTO learning_history (user_id, word_id, activity_type, is_correct, created_at)
VALUES
  (@admin_user_id, @word1, 'LEARN', TRUE, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  (@admin_user_id, @word1, 'REVIEW', TRUE, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (@admin_user_id, @word1, 'REVIEW', TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (@admin_user_id, @word1, 'REVIEW', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (@admin_user_id, @word2, 'LEARN', TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (@admin_user_id, @word2, 'REVIEW', TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (@admin_user_id, @word2, 'REVIEW', FALSE, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (@admin_user_id, @word3, 'LEARN', TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (@admin_user_id, @word3, 'REVIEW', TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (@admin_user_id, @word3, 'REVIEW', FALSE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (@admin_user_id, @word4, 'LEARN', FALSE, NOW());

-- ============================================================
-- 6. THÊM AI CONVERSATION (Hội thoại với AI)
-- ============================================================

INSERT IGNORE INTO ai_conversations (user_id, type, title, created_at, updated_at)
VALUES 
  (@admin_user_id, 'VOCABULARY', 'Learn about abandon', NOW(), NOW()),
  (@admin_user_id, 'GENERAL', 'English learning tips', NOW(), NOW());

-- Lấy conversation ID
SET @conv1 = (SELECT id FROM ai_conversations WHERE user_id = @admin_user_id AND type = 'VOCABULARY' LIMIT 1);

-- ============================================================
-- 7. THÊM AI MESSAGES (Tin nhắn AI)
-- ============================================================

INSERT IGNORE INTO ai_messages (conversation_id, role, content, tokens_used, response_time_ms, created_at)
VALUES
  (@conv1, 'USER', 'What does abandon mean?', 15, 100, NOW()),
  (@conv1, 'ASSISTANT', 'Abandon means to leave someone or something behind, often permanently. For example: "They abandoned the project due to lack of funding."', 45, 1500, NOW());

-- ============================================================
-- 8. THÊM RATE LIMIT (Giới hạn API)
-- ============================================================

INSERT IGNORE INTO rate_limits (user_id, tokens_used, request_count, period_start, period_end, created_at, updated_at)
VALUES (
  @admin_user_id,
  100,
  5,
  DATE_SUB(NOW(), INTERVAL 12 HOUR),
  DATE_ADD(NOW(), INTERVAL 12 HOUR),
  NOW(),
  NOW()
);

-- ============================================================
-- THÔNG TIN TÀI KHOẢN ADMIN
-- ============================================================
-- Username: admin
-- Email: admin@vocabularyai.com
-- Password: Admin@123
-- Role: ADMIN
-- Status: ACTIVE
-- 
-- ⚠️ LƯU Ý QUAN TRỌNG:
-- Password hash ở trên là GIẢ. Để đăng nhập được, bạn cần:
-- 1. Tạo real BCrypt hash cho mật khẩu "Admin@123"
-- 2. Hoặc đổi password qua API /api/auth/register
-- 
-- CÁCH KHẮC PHỤC:
-- Option 1: Sử dụng script Java để generate BCrypt hash
--   import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
--   new BCryptPasswordEncoder().encode("Admin@123");
--   
-- Option 2: Tạo tài khoản mới qua API
--   POST /api/auth/register
--   {"username":"admin", "email":"admin@vocabularyai.com", "password":"Admin@123", "fullName":"Administrator"}
-- 
-- ============================================================
-- HƯỚNG DẪN TEST:
-- ============================================================
-- 
-- 1. ĐĂNG NHẬP:
--    POST /api/auth/login
--    Body: {"username":"admin", "password":"Admin@123"}
--    Response: {"accessToken":"...", "refreshToken":"..."}
-- 
-- 2. TEST TASK 2 (Phân tích Điểm yếu/Mạnh):
--    GET /api/ai/personalized/weakness-strength
--    Header: Authorization: Bearer {accessToken}
--    Response: {
--      "weaknesses": [...],
--      "strengths": [...],
--      "priorityActions": [...]
--    }
-- 
-- 3. TEST TASK 3 (Gợi ý Cá nhân hóa):
--    GET /api/ai/personalized/recommendations
--    Header: Authorization: Bearer {accessToken}
--    Response: {
--      "recommendations": [...],
--      "quickWins": [...],
--      "weeklySchedule": [...]
--    }
-- 
-- 4. TEST Vocabulary (Giải thích từ):
--    POST /api/ai/vocabulary/explain
--    Body: {"wordId":1}
--    Response: {"word":"abandon", "meaning":"...", "examples":["..."]}
-- 
-- ============================================================
