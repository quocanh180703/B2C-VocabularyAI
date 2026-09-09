-- ============================================================
-- V11__update_admin_password.sql
-- Cập nhật mật khẩu admin bằng BCrypt hash thực
-- ============================================================

UPDATE users 
SET password = '$2a$10$Xz7LBW5LIm/RoVMwsB5H2uNVVk3i8KZvJzq8Z9qhR5xL5Zk1OqYVa'
WHERE username = 'admin' AND email = 'admin@vocabularyai.com';
