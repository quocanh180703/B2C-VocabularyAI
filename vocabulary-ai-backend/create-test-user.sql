-- Create test user directly for E2E testing
-- Password: TestPassword@123 (BCrypt hashed)
-- Hash: $2a$12$wfJ6YvhV8JlhzQc4RBo3JuWMpT7KqDRnVzI0GqMqM6w3KqVvNS1bW

INSERT INTO users (username, email, password, full_name, role, status, created_at, updated_at)
VALUES (
    'testuser_e2e',
    'testuser_e2e@example.com',
    '$2a$12$wfJ6YvhV8JlhzQc4RBo3JuWMpT7KqDRnVzI0GqMqM6w3KqVvNS1bW',
    'E2E Test User',
    'STUDENT',
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Get latest OTP for register test
SELECT 
    otp_code, 
    email, 
    otp_type,
    created_at,
    expires_at,
    is_expired(expires_at) as is_expired
FROM otp_verifications 
WHERE email LIKE 'testuser%' 
AND otp_type = 'REGISTER'
ORDER BY created_at DESC 
LIMIT 1;
