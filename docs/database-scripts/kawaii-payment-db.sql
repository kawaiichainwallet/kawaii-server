-- ================================================================
-- KawaiiChain Wallet - 支付服务数据库 (kawaii-payment-db)
-- 负责：支付处理、生活缴费
-- ================================================================

-- 启用UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- 1. 支付订单表 (payment_orders)
-- ================================================================
CREATE TABLE payment_orders (
    order_id BIGINT PRIMARY KEY,
    merchant_id BIGINT NOT NULL, -- 注意：不再是外键，而是引用商户服务的商户ID

    -- 订单信息
    merchant_order_id VARCHAR(100) NOT NULL, -- 商户方订单号
    amount DECIMAL(20, 8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    description TEXT,

    -- 支付信息
    payment_address VARCHAR(100),
    token_id BIGINT, -- 注意：不再是外键，而是引用核心服务的代币ID
    exchange_rate DECIMAL(20, 8), -- 法币汇率
    actual_amount DECIMAL(20, 8), -- 实际支付金额

    -- 支付状态
    status VARCHAR(20) DEFAULT 'created' CHECK (status IN
        ('created', 'pending', 'paid', 'overpaid', 'underpaid', 'expired', 'cancelled', 'refunded')),

    -- 时间配置
    expires_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,

    -- 回调配置
    callback_url VARCHAR(500),
    return_url VARCHAR(500),
    callback_attempts INTEGER DEFAULT 0,
    callback_success BOOLEAN DEFAULT FALSE,

    -- 关联交易
    payment_tx_id BIGINT, -- 注意：不再是外键，而是引用核心服务的交易ID
    refund_tx_id BIGINT, -- 注意：不再是外键，而是引用核心服务的交易ID

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_orders_merchant_id ON payment_orders(merchant_id);
CREATE INDEX idx_payment_orders_status ON payment_orders(status);
CREATE INDEX idx_payment_orders_created_at ON payment_orders(created_at);
CREATE UNIQUE INDEX idx_payment_orders_merchant_order ON payment_orders(merchant_id, merchant_order_id);

-- ================================================================
-- 2. 缴费服务商表 (bill_providers)
-- ================================================================
CREATE TABLE bill_providers (
    provider_id BIGINT PRIMARY KEY,

    -- 服务商信息
    provider_name VARCHAR(200) NOT NULL,
    provider_code VARCHAR(50) UNIQUE NOT NULL,
    service_type VARCHAR(50) NOT NULL, -- water, electricity, gas, telecom, internet

    -- 服务区域
    supported_regions TEXT[],
    service_url VARCHAR(500),

    -- API配置
    api_endpoint VARCHAR(500),
    api_credentials JSONB, -- 加密存储API凭证

    -- 费用配置
    service_fee_rate DECIMAL(5, 4) DEFAULT 0,
    service_fee_fixed DECIMAL(10, 2) DEFAULT 0,

    -- 状态管理
    is_active BOOLEAN DEFAULT TRUE,
    maintenance_mode BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bill_providers_type ON bill_providers(service_type);
CREATE INDEX idx_bill_providers_active ON bill_providers(is_active);

-- ================================================================
-- 3. 缴费记录表 (bill_payments)
-- ================================================================
CREATE TABLE bill_payments (
    payment_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 注意：不再是外键，而是引用用户服务的用户ID
    provider_id BIGINT NOT NULL REFERENCES bill_providers(provider_id),

    -- 账单信息
    account_number VARCHAR(100) NOT NULL, -- 用户在服务商的账号
    bill_amount DECIMAL(10, 2) NOT NULL,
    service_fee DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,

    -- 支付信息
    payment_method VARCHAR(20) DEFAULT 'crypto', -- crypto, balance
    transaction_id BIGINT, -- 注意：不再是外键，而是引用核心服务的交易ID

    -- 账单详情
    billing_period VARCHAR(20), -- 账单周期
    due_date DATE,
    bill_details JSONB, -- 账单明细

    -- 处理状态
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN
        ('pending', 'processing', 'success', 'failed', 'refunded')),

    -- 第三方信息
    provider_order_id VARCHAR(100), -- 服务商订单号
    provider_response JSONB, -- 服务商响应

    -- 时间信息
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bill_payments_user_id ON bill_payments(user_id);
CREATE INDEX idx_bill_payments_status ON bill_payments(status);
CREATE INDEX idx_bill_payments_created_at ON bill_payments(created_at);

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

CREATE TRIGGER update_payment_orders_updated_at BEFORE UPDATE ON payment_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bill_providers_updated_at BEFORE UPDATE ON bill_providers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bill_payments_updated_at BEFORE UPDATE ON bill_payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================