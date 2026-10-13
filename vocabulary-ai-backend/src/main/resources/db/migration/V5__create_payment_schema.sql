-- ============================================================
-- VocabularyAI - Payment Module Schema
-- V3__create_payment_schema.sql
-- 
-- Payment schema for PayOS integration
-- Tables:
-- - packages: Subscription packages available for purchase
-- - payment_orders: Payment orders created by users
-- - payments: Actual payment transactions from PayOS
-- - subscriptions: User subscription entitlements
--
-- Database: MySQL 8.x+
-- Engine: InnoDB
-- Charset: utf8mb4
-- ============================================================

-- ============================================================
-- 1. PACKAGES TABLE
-- ============================================================
-- Subscription packages available for purchase (V1: PREMIUM_MONTHLY, PREMIUM_YEARLY)
CREATE TABLE packages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(19, 2) NOT NULL,
  duration_days INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_package_code UNIQUE (code),
  INDEX idx_package_status (status),
  INDEX idx_package_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. PAYMENT_ORDERS TABLE
-- ============================================================
-- Payment orders created by users
-- Lifecycle: PENDING -> PAID | FAILED | CANCELLED
CREATE TABLE payment_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  package_id BIGINT NOT NULL,
  order_code VARCHAR(50) NOT NULL,
  amount DECIMAL(19, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  payment_url TEXT,
  expired_at DATETIME,
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_order_code UNIQUE (order_code),
  CONSTRAINT fk_payment_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_payment_order_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_order_user_id (user_id),
  INDEX idx_order_status (status),
  INDEX idx_order_created_at (created_at),
  INDEX idx_order_code (order_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. PAYMENTS TABLE
-- ============================================================
-- Actual payment transactions from PayOS
-- Lifecycle: PENDING -> SUCCESS | FAILED | CANCELLED
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_order_id BIGINT NOT NULL,
  transaction_code VARCHAR(100) NOT NULL,
  amount DECIMAL(19, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  payment_method VARCHAR(50),
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_payment_order_id UNIQUE (payment_order_id),
  CONSTRAINT uk_transaction_code UNIQUE (transaction_code),
  CONSTRAINT fk_payment_order FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_payment_order_id (payment_order_id),
  INDEX idx_payment_status (status),
  INDEX idx_payment_transaction_code (transaction_code),
  INDEX idx_payment_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. SUBSCRIPTIONS TABLE
-- ============================================================
-- User subscription entitlements
-- Lifecycle: ACTIVE -> EXPIRED | CANCELLED | SUSPENDED
-- UK constraint ensures one active subscription per user
CREATE TABLE subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  package_id BIGINT NOT NULL,
  payment_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  start_at DATETIME NOT NULL,
  end_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_subscription_user_id UNIQUE (user_id),
  CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_subscription_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_subscription_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_subscription_user_id (user_id),
  INDEX idx_subscription_status (status),
  INDEX idx_subscription_end_at (end_at),
  INDEX idx_subscription_package_id (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. INSERT INITIAL DATA (V1 Packages)
-- ============================================================
-- V1 Packages: PREMIUM_MONTHLY (99,000 VND/month) and PREMIUM_YEARLY (899,000 VND/year)
INSERT INTO packages (code, name, price, duration_days, status, description, created_at, updated_at) VALUES
('PREMIUM_MONTHLY', 'Premium Monthly', 99000, 30, 'ACTIVE', 'Premium subscription for 1 month', NOW(), NOW()),
('PREMIUM_YEARLY', 'Premium Yearly', 899000, 365, 'ACTIVE', 'Premium subscription for 1 year', NOW(), NOW());
