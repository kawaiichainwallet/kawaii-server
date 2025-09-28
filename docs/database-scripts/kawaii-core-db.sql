-- ================================================================
-- KawaiiChain Wallet - 钱包核心服务数据库 (kawaii-core-db)
-- 负责：钱包管理、区块链资产、链上交易
-- ================================================================

-- ================================================================
-- 1. 支持的区块链网络表 (supported_chains)
-- ================================================================
CREATE TABLE supported_chains (
    chain_id BIGINT PRIMARY KEY,
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

-- ================================================================
-- 2. 钱包表 (wallets)
-- ================================================================
CREATE TABLE wallets (
    wallet_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 注意：不再是外键，而是引用用户服务的用户ID

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

-- ================================================================
-- 3. 钱包地址表 (wallet_addresses)
-- ================================================================
CREATE TABLE wallet_addresses (
    address_id BIGINT PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    chain_id BIGINT NOT NULL REFERENCES supported_chains(chain_id),

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

-- ================================================================
-- 4. 代币配置表 (tokens)
-- ================================================================
CREATE TABLE tokens (
    token_id BIGINT PRIMARY KEY,
    chain_id BIGINT NOT NULL REFERENCES supported_chains(chain_id),

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

-- ================================================================
-- 5. 交易记录表 (transactions)
-- ================================================================
CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 注意：不再是外键，而是引用用户服务的用户ID
    wallet_id BIGINT NOT NULL REFERENCES wallets(wallet_id),
    chain_id BIGINT NOT NULL REFERENCES supported_chains(chain_id),

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
    token_id BIGINT REFERENCES tokens(token_id),
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

-- 索引 (支持按月分区)
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_hash ON transactions(tx_hash);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_addresses ON transactions(from_address, to_address);

-- ================================================================
-- 6. 交易日志表 (transaction_logs)
-- ================================================================
CREATE TABLE transaction_logs (
    log_id BIGINT PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,

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

CREATE TRIGGER update_supported_chains_updated_at BEFORE UPDATE ON supported_chains
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wallet_addresses_updated_at BEFORE UPDATE ON wallet_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tokens_updated_at BEFORE UPDATE ON tokens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================