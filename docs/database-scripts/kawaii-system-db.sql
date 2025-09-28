-- ================================================================
-- KawaiiChain Wallet - 系统配置数据库 (kawaii-system-db)
-- 负责：系统配置、审计追踪（由Gateway管理）
-- ================================================================


-- ================================================================
-- 1. 系统配置表 (system_configs)
-- ================================================================
CREATE TABLE system_configs (
    config_id BIGINT PRIMARY KEY,

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
    updated_by BIGINT -- 注意：不再是外键，而是引用用户服务的用户ID
);

CREATE INDEX idx_system_configs_group ON system_configs(config_group);

-- ================================================================
-- 2. 审计日志表 (audit_logs)
-- ================================================================
CREATE TABLE audit_logs (
    log_id BIGINT PRIMARY KEY,

    -- 操作信息
    user_id BIGINT, -- 注意：不再是外键，而是引用用户服务的用户ID
    action VARCHAR(100) NOT NULL, -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT等
    resource_type VARCHAR(50) NOT NULL, -- user, wallet, transaction等
    resource_id BIGINT,

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

-- 索引 (支持按月分区)
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

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

CREATE TRIGGER update_system_configs_updated_at BEFORE UPDATE ON system_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================