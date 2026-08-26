-- Cleanup Speaking tables trước khi chạy migration V9
-- Chạy script này trực tiếp trên MySQL

DROP TABLE IF EXISTS speaking_feedback_v8;
DROP TABLE IF EXISTS speaking_attempts;
DROP TABLE IF EXISTS speaking_messages;

-- Kiểm tra
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'datn_2' AND TABLE_NAME LIKE 'speaking%';
