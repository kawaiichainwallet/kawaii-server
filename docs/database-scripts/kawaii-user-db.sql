-- ================================================================
-- KawaiiChain Wallet - 用户服务数据库 (kawaii-user-db)
-- 负责：用户身份管理、认证授权、个人资料、安全控制
--
-- 【时间字段约定】
-- 所有时间字段使用 TIMESTAMP 类型（不带时区），统一存储 UTC 时间
-- 应用层负责时区转换，确保写入数据库的时间都是 UTC
-- ================================================================

-- ================================================================
-- 1. 用户基础表 (users)
-- ================================================================
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL, -- BCrypt加密存储，包含内置盐值

    -- 状态管理
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'suspended', 'deleted')),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,

    -- 安全设置
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(15),

    -- 元数据（UTC时间）
    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    created_by BIGINT,
    updated_by BIGINT
);

-- 索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ================================================================
-- 2. 用户资料表 (user_profiles)
-- ================================================================
CREATE TABLE user_profiles (
    profile_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- 个人信息
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    birth_date DATE,
    gender VARCHAR(10) CHECK (gender IN ('male', 'female', 'other', 'prefer_not_say')),

    -- 地址信息
    country VARCHAR(3), -- ISO 3166-1 alpha-3
    state_province VARCHAR(100),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),

    -- 偏好设置
    language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(10) DEFAULT 'USD',
    notifications_enabled BOOLEAN DEFAULT TRUE,

    -- 元数据（UTC时间）
    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE UNIQUE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- ================================================================
-- 3. KYC认证表 (user_kyc)
-- ================================================================
CREATE TABLE user_kyc (
    kyc_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- 认证级别
    kyc_level INTEGER DEFAULT 0 CHECK (kyc_level >= 0 AND kyc_level <= 3),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'expired')),

    -- 身份证件信息 (加密存储)
    id_type VARCHAR(20), -- passport, id_card, driver_license
    id_number_encrypted BYTEA,
    id_front_url VARCHAR(500),
    id_back_url VARCHAR(500),
    selfie_url VARCHAR(500),

    -- 审核信息（UTC时间）
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    rejection_reason TEXT,
    expires_at TIMESTAMP,

    -- 额外文档
    additional_docs JSONB,

    -- 元数据（UTC时间）
    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE UNIQUE INDEX idx_user_kyc_user_id ON user_kyc(user_id);

-- ================================================================
-- 4. JWT黑名单表 (jwt_blacklist)
-- ================================================================
CREATE TABLE jwt_blacklist (
    id BIGSERIAL PRIMARY KEY,
    token_id VARCHAR(100) NOT NULL UNIQUE, -- JWT的jti claim
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    token_type VARCHAR(20) DEFAULT 'access' CHECK (token_type IN ('access', 'refresh')),
    expires_at TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    reason VARCHAR(100) DEFAULT 'logout' -- logout, force_logout, security
);

-- 索引
CREATE INDEX idx_jwt_blacklist_token_id ON jwt_blacklist(token_id);
CREATE INDEX idx_jwt_blacklist_user_id ON jwt_blacklist(user_id);

-- ================================================================
-- 自动更新时间戳触发器（UTC时间）
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = (NOW() AT TIME ZONE 'UTC');
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Leaf框架专用的更新时间触发器函数（UTC时间）
CREATE OR REPLACE FUNCTION update_leaf_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = (NOW() AT TIME ZONE 'UTC');
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_kyc_updated_at BEFORE UPDATE ON user_kyc
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 5. Leaf分布式ID分配表 (leaf_alloc)
-- ================================================================
CREATE TABLE leaf_alloc (
    biz_tag VARCHAR(128) NOT NULL DEFAULT '',
    max_id BIGINT NOT NULL DEFAULT 1,
    step INT NOT NULL,
    description VARCHAR(256) DEFAULT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    PRIMARY KEY (biz_tag)
);

-- 初始化各业务线ID分配策略
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES
('user-id', 100000, 1000, '用户相关ID'),
('wallet-id', 200000, 1000, '钱包相关ID'),
('transaction-id', 300000, 5000, '交易相关ID'),
('payment-id', 400000, 3000, '支付订单ID'),
('merchant-id', 500000, 500, '商户相关ID'),
('notification-id', 600000, 2000, '通知相关ID');

-- 创建更新时间触发器
CREATE TRIGGER update_leaf_alloc_update_time BEFORE UPDATE ON leaf_alloc
    FOR EACH ROW EXECUTE FUNCTION update_leaf_update_time_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================