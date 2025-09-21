# 数据库设计

## 数据库概览

### 技术选型

| 属性 | 值 |
|------|---|
| **数据库** | PostgreSQL 17 |
| **字符编码** | UTF8 |
| **时区** | UTC |
| **备份策略** | 定期全量 + 增量备份 |

### 设计原则

- **数据安全**: 敏感信息加密存储，支持字段级加密
- **高性能**: 合理设计索引，支持分区表
- **可扩展**: 支持水平分片，预留扩展字段
- **审计追踪**: 完整的操作日志记录
- **合规性**: 支持数据脱敏和GDPR合规

### 🗃️ 数据库分离

- **用户服务**: 用户信息、认证数据
- **钱包服务**: 钱包、交易记录
- **支付服务**: 支付订单、商户信息

### 🔧 数据库特性

- **软删除**: 使用 `deleted` 字段标记删除
- **审计字段**: 自动维护创建时间、更新时间
- **连接池**: HikariCP高性能连接池
- **事务管理**: 基于Spring的声明式事务

## 核心数据表设计

### 1. 用户管理模块

#### 1.1 用户基础表 (users)

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
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
    locked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_login_ip INET,
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- 索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
```

#### 1.2 用户资料表 (user_profiles)

```sql
CREATE TABLE user_profiles (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
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
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

#### 1.3 KYC认证表 (user_kyc)

```sql
CREATE TABLE user_kyc (
    kyc_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 认证级别
    kyc_level INTEGER DEFAULT 0 CHECK (kyc_level >= 0 AND kyc_level <= 3),
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'expired')),
    
    -- 身份证件信息 (加密存储)
    id_type VARCHAR(20), -- passport, id_card, driver_license
    id_number_encrypted BYTEA,
    id_front_url VARCHAR(500),
    id_back_url VARCHAR(500),
    selfie_url VARCHAR(500),
    
    -- 审核信息
    submitted_at TIMESTAMP WITH TIME ZONE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID,
    rejection_reason TEXT,
    expires_at TIMESTAMP WITH TIME ZONE,
    
    -- 额外文档
    additional_docs JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_user_kyc_user_id ON user_kyc(user_id);
CREATE INDEX idx_user_kyc_status ON user_kyc(status);
CREATE INDEX idx_user_kyc_level ON user_kyc(kyc_level);
```

### 2. 钱包管理模块

#### 2.1 支持的区块链网络 (supported_chains)

```sql
CREATE TABLE supported_chains (
    chain_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chain_name VARCHAR(50) NOT NULL UNIQUE, -- ethereum, bitcoin, bsc
    chain_symbol VARCHAR(10) NOT NULL, -- ETH, BTC, BNB
    network_id INTEGER, -- 网络ID，如以太坊主网是1
    
    -- 网络配置
    rpc_urls TEXT[] NOT NULL,
    explorer_urls TEXT[],
    native_currency_symbol VARCHAR(10),
    native_currency_decimals INTEGER DEFAULT 18,
    
    -- 功能开关
    is_active BOOLEAN DEFAULT TRUE,
    supports_erc20 BOOLEAN DEFAULT FALSE,
    supports_nft BOOLEAN DEFAULT FALSE,
    
    -- 交易配置
    min_confirmations INTEGER DEFAULT 6,
    gas_price_levels JSONB, -- {"slow": 10, "standard": 20, "fast": 30}
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_supported_chains_active ON supported_chains(is_active);
```

#### 2.2 钱包表 (wallets)

```sql
CREATE TABLE wallets (
    wallet_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- 钱包基本信息
    wallet_name VARCHAR(100) NOT NULL,
    wallet_type VARCHAR(20) DEFAULT 'hd' CHECK (wallet_type IN ('hd', 'simple', 'multisig')),
    
    -- 助记词相关 (加密存储)
    mnemonic_encrypted BYTEA,
    seed_encrypted BYTEA,
    derivation_path VARCHAR(100) DEFAULT 'm/44''/60''/0''/0', -- BIP44 path
    
    -- 安全设置
    encryption_method VARCHAR(20) DEFAULT 'aes256',
    password_hint VARCHAR(200),
    backup_completed BOOLEAN DEFAULT FALSE,
    
    -- 状态管理
    is_active BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_active ON wallets(is_active);
CREATE UNIQUE INDEX idx_wallets_user_default ON wallets(user_id) WHERE is_default = TRUE;
```

#### 2.3 钱包地址表 (wallet_addresses)

```sql
CREATE TABLE wallet_addresses (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    chain_id UUID NOT NULL REFERENCES supported_chains(chain_id),
    
    -- 地址信息
    address VARCHAR(100) NOT NULL,
    address_index INTEGER DEFAULT 0, -- HD钱包地址索引
    private_key_encrypted BYTEA, -- 加密的私钥
    public_key VARCHAR(200),
    
    -- 余额缓存 (定期更新)
    balance_cache DECIMAL(36, 18) DEFAULT 0,
    balance_updated_at TIMESTAMP WITH TIME ZONE,
    
    -- 状态管理
    is_active BOOLEAN DEFAULT TRUE,
    label VARCHAR(100), -- 用户自定义标签
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_wallet_addresses_chain_address ON wallet_addresses(chain_id, address);
CREATE INDEX idx_wallet_addresses_wallet_id ON wallet_addresses(wallet_id);
CREATE INDEX idx_wallet_addresses_active ON wallet_addresses(is_active);
```

#### 2.4 代币配置表 (tokens)

```sql
CREATE TABLE tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chain_id UUID NOT NULL REFERENCES supported_chains(chain_id),
    
    -- 代币基本信息
    contract_address VARCHAR(100),
    token_symbol VARCHAR(20) NOT NULL,
    token_name VARCHAR(100) NOT NULL,
    decimals INTEGER NOT NULL DEFAULT 18,
    
    -- 显示配置
    logo_url VARCHAR(500),
    is_popular BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    
    -- 状态管理
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    
    -- 价格信息缓存
    price_usd DECIMAL(20, 8),
    price_updated_at TIMESTAMP WITH TIME ZONE,
    market_cap DECIMAL(30, 2),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_tokens_chain_contract ON tokens(chain_id, contract_address);
CREATE INDEX idx_tokens_symbol ON tokens(token_symbol);
CREATE INDEX idx_tokens_active ON tokens(is_active);
```

### 3. 交易管理模块

#### 3.1 交易记录表 (transactions)

```sql
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    wallet_id UUID NOT NULL REFERENCES wallets(wallet_id),
    chain_id UUID NOT NULL REFERENCES supported_chains(chain_id),
    
    -- 交易基本信息
    tx_hash VARCHAR(100) UNIQUE,
    block_number BIGINT,
    block_hash VARCHAR(100),
    transaction_index INTEGER,
    
    -- 交易类型和方向
    tx_type VARCHAR(20) NOT NULL CHECK (tx_type IN ('send', 'receive', 'contract', 'swap')),
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('in', 'out')),
    
    -- 地址信息
    from_address VARCHAR(100) NOT NULL,
    to_address VARCHAR(100) NOT NULL,
    
    -- 金额信息
    token_id UUID REFERENCES tokens(token_id),
    amount DECIMAL(36, 18) NOT NULL,
    fee_amount DECIMAL(36, 18) DEFAULT 0,
    gas_used BIGINT,
    gas_price BIGINT,
    
    -- 交易状态
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'failed', 'dropped')),
    confirmations INTEGER DEFAULT 0,
    
    -- 元数据
    nonce BIGINT,
    input_data TEXT,
    memo VARCHAR(500),
    tags TEXT[], -- 用户自定义标签
    
    -- 时间信息
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 分区表 (按月分区)
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_hash ON transactions(tx_hash);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_addresses ON transactions(from_address, to_address);
```

#### 3.2 交易日志表 (transaction_logs)

```sql
CREATE TABLE transaction_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    
    -- 日志信息
    log_index INTEGER,
    log_data TEXT,
    topics TEXT[],
    
    -- 合约相关
    contract_address VARCHAR(100),
    event_name VARCHAR(100),
    decoded_data JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transaction_logs_tx_id ON transaction_logs(transaction_id);
CREATE INDEX idx_transaction_logs_contract ON transaction_logs(contract_address);
```

### 4. 商户支付模块

#### 4.1 商户表 (merchants)

```sql
CREATE TABLE merchants (
    merchant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    
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
```

#### 4.2 支付订单表 (payment_orders)

```sql
CREATE TABLE payment_orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL REFERENCES merchants(merchant_id),
    
    -- 订单信息
    merchant_order_id VARCHAR(100) NOT NULL, -- 商户方订单号
    amount DECIMAL(20, 8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    description TEXT,
    
    -- 支付信息
    payment_address VARCHAR(100),
    token_id UUID REFERENCES tokens(token_id),
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
    payment_tx_id UUID REFERENCES transactions(transaction_id),
    refund_tx_id UUID REFERENCES transactions(transaction_id),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_orders_merchant_id ON payment_orders(merchant_id);
CREATE INDEX idx_payment_orders_status ON payment_orders(status);
CREATE INDEX idx_payment_orders_created_at ON payment_orders(created_at);
CREATE UNIQUE INDEX idx_payment_orders_merchant_order ON payment_orders(merchant_id, merchant_order_id);
```

### 5. 生活缴费模块

#### 5.1 缴费服务商表 (bill_providers)

```sql
CREATE TABLE bill_providers (
    provider_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
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
```

#### 5.2 缴费记录表 (bill_payments)

```sql
CREATE TABLE bill_payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    provider_id UUID NOT NULL REFERENCES bill_providers(provider_id),
    
    -- 账单信息
    account_number VARCHAR(100) NOT NULL, -- 用户在服务商的账号
    bill_amount DECIMAL(10, 2) NOT NULL,
    service_fee DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- 支付信息
    payment_method VARCHAR(20) DEFAULT 'crypto', -- crypto, balance
    transaction_id UUID REFERENCES transactions(transaction_id),
    
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
```

### 6. 通知系统

#### 6.1 通知表 (notifications)

```sql
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    
    -- 通知内容
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL, -- transaction, system, security, promotion
    
    -- 通知渠道
    channels TEXT[] DEFAULT ARRAY['in_app'], -- in_app, email, sms, push
    
    -- 关联信息
    related_id UUID, -- 关联的交易ID、订单ID等
    related_type VARCHAR(50), -- transaction, payment_order, bill_payment
    
    -- 状态管理
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP WITH TIME ZONE,
    
    -- 元数据
    metadata JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

### 7. 系统配置与审计

#### 7.1 系统配置表 (system_configs)

```sql
CREATE TABLE system_configs (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- 配置信息
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(20) DEFAULT 'string' CHECK (config_type IN ('string', 'number', 'boolean', 'json')),
    
    -- 配置分组
    config_group VARCHAR(50) DEFAULT 'general',
    description TEXT,
    
    -- 权限控制
    is_public BOOLEAN DEFAULT FALSE, -- 是否可公开访问
    is_encrypted BOOLEAN DEFAULT FALSE, -- 值是否加密存储
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(user_id)
);

CREATE INDEX idx_system_configs_group ON system_configs(config_group);
CREATE INDEX idx_system_configs_public ON system_configs(is_public);
```

#### 7.2 审计日志表 (audit_logs)

```sql
CREATE TABLE audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- 操作信息
    user_id UUID REFERENCES users(user_id),
    action VARCHAR(100) NOT NULL, -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT等
    resource_type VARCHAR(50) NOT NULL, -- user, wallet, transaction等
    resource_id UUID,
    
    -- 请求信息
    ip_address INET,
    user_agent TEXT,
    request_path VARCHAR(500),
    request_method VARCHAR(10),
    
    -- 操作详情
    old_values JSONB, -- 修改前的值
    new_values JSONB, -- 修改后的值
    metadata JSONB, -- 额外元数据
    
    -- 操作结果
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 分区表 (按月分区)
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

### 8. JWT黑名单管理

#### 8.1 JWT黑名单表 (jwt_blacklist)

```sql
CREATE TABLE jwt_blacklist
(
    id             BIGSERIAL PRIMARY KEY,
    token_id       VARCHAR(100)             NOT NULL UNIQUE, -- JWT的jti claim
    user_id        UUID                     NOT NULL REFERENCES users (user_id),
    token_type     VARCHAR(20)              DEFAULT 'access' CHECK (token_type IN ('access', 'refresh')),
    expires_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    blacklisted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reason         VARCHAR(100)             DEFAULT 'logout' -- logout, force_logout, security
);

-- 索引
CREATE INDEX idx_jwt_blacklist_token_id ON jwt_blacklist (token_id);
CREATE INDEX idx_jwt_blacklist_user_id ON jwt_blacklist (user_id);
CREATE INDEX idx_jwt_blacklist_expires_at ON jwt_blacklist (expires_at);
CREATE INDEX idx_jwt_blacklist_active ON jwt_blacklist (expires_at);
```
