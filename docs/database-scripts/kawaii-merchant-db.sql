-- ================================================================
-- KawaiiChain Wallet - 商户服务数据库 (kawaii-merchant-db)
-- 负责：商户管理、API配置
-- ================================================================

-- 启用UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- 1. 商户表 (merchants)
-- ================================================================
CREATE TABLE merchants (
    merchant_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 注意：不再是外键，而是引用用户服务的用户ID

    -- 商户信息
    merchant_name VARCHAR(200) NOT NULL,
    business_type VARCHAR(100),
    website_url VARCHAR(500),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),

    -- API配置
    api_key VARCHAR(100) UNIQUE NOT NULL,
    api_secret_hash VARCHAR(255),
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(100),

    -- 结算配置
    settlement_currency VARCHAR(10) DEFAULT 'USD',
    settlement_address VARCHAR(100),
    auto_settlement BOOLEAN DEFAULT FALSE,
    settlement_threshold DECIMAL(20, 8) DEFAULT 0,

    -- 费率配置
    fee_rate DECIMAL(5, 4) DEFAULT 0.0025, -- 0.25%
    min_fee DECIMAL(10, 8) DEFAULT 0,
    max_fee DECIMAL(10, 8),

    -- 限额配置
    daily_limit DECIMAL(20, 8),
    monthly_limit DECIMAL(20, 8),
    single_tx_limit DECIMAL(20, 8),

    -- 状态管理
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'active', 'suspended', 'inactive')),
    kyc_verified BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchants_user_id ON merchants(user_id);
CREATE INDEX idx_merchants_status ON merchants(status);
CREATE UNIQUE INDEX idx_merchants_api_key ON merchants(api_key);

-- ================================================================
-- 自动更新时间戳触发器
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_merchants_updated_at BEFORE UPDATE ON merchants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================