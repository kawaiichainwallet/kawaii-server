-- ================================================================
-- KawaiiChain Wallet - 管理后台数据库 (kawaii-admin-db)
-- 负责：系统配置、运营审计、管理员权限（由kawaii-admin-service管理）
--
-- 【时间字段约定】
-- 所有时间字段使用 TIMESTAMP 类型（不带时区），统一存储 UTC 时间
-- 应用层负责时区转换，确保写入数据库的时间都是 UTC
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

    -- 元数据（UTC时间）
    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_by BIGINT
);

CREATE INDEX idx_system_configs_group ON system_configs(config_group);

-- ================================================================
-- 2. 运营审计日志表 (audit_logs)
-- 专门记录运营级别的重要操作，C端用户行为通过ELK收集
-- ================================================================
CREATE TABLE audit_logs (
    log_id BIGINT PRIMARY KEY,

    -- 操作信息
    user_id BIGINT,
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

    -- 元数据（UTC时间）
    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC')
);

-- 索引 (支持按月分区)
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- ================================================================
-- 3. 管理员用户表 (admin_users)
-- ================================================================
CREATE TABLE admin_users (
    admin_id BIGINT PRIMARY KEY,

    -- 基本信息
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,

    -- 个人信息
    real_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50),
    department VARCHAR(100),
    position VARCHAR(100),

    -- 状态管理
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'suspended')),

    -- 安全设置
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip INET,

    -- 权限控制
    is_super_admin BOOLEAN DEFAULT FALSE,
    permissions TEXT[], -- 额外权限列表

    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_admin_users_username ON admin_users(username);
CREATE INDEX idx_admin_users_email ON admin_users(email);
CREATE INDEX idx_admin_users_status ON admin_users(status);

-- ================================================================
-- 4. 管理员角色表 (admin_roles)
-- ================================================================
CREATE TABLE admin_roles (
    role_id BIGINT PRIMARY KEY,

    -- 角色基本信息
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,

    -- 权限配置
    permissions JSONB NOT NULL DEFAULT '[]', -- 权限列表JSON数组
    menu_permissions JSONB DEFAULT '[]', -- 菜单权限

    -- 状态管理
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_admin_roles_code ON admin_roles(role_code);
CREATE INDEX idx_admin_roles_active ON admin_roles(is_active);

-- ================================================================
-- 5. 管理员角色关联表 (admin_user_roles)
-- ================================================================
CREATE TABLE admin_user_roles (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES admin_users(admin_id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES admin_roles(role_id) ON DELETE CASCADE,

    -- 分配信息
    assigned_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    assigned_by BIGINT,
    expires_at TIMESTAMP, -- 角色过期时间（可选）

    UNIQUE(admin_id, role_id)
);

CREATE INDEX idx_admin_user_roles_admin ON admin_user_roles(admin_id);
CREATE INDEX idx_admin_user_roles_role ON admin_user_roles(role_id);

-- ================================================================
-- 初始化管理员角色数据
-- ================================================================
-- 注意：admin_id 和 role_id 需要调用 kawaii-user 服务的 Leaf ID 生成器获取
-- 这里使用临时ID，实际部署时需要通过API调用获取正确的分布式ID
INSERT INTO admin_roles(role_id, role_name, role_code, description, permissions) VALUES
(700001, '超级管理员', 'SUPER_ADMIN', '拥有所有权限的超级管理员', '["*"]'),
(700002, '系统管理员', 'SYSTEM_ADMIN', '系统配置和用户管理', '["system:config", "user:manage", "audit:view"]'),
(700003, '财务管理员', 'FINANCE_ADMIN', '财务数据和交易管理', '["finance:view", "transaction:manage", "report:export"]'),
(700004, '客服管理员', 'CUSTOMER_SERVICE', '客户服务和支持', '["user:view", "support:manage", "ticket:handle"]'),
(700005, '只读管理员', 'READ_ONLY', '只能查看数据，无修改权限', '["data:view", "report:view"]');

-- ================================================================
-- 自动更新时间戳触发器
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = (NOW() AT TIME ZONE 'UTC');
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_system_configs_updated_at BEFORE UPDATE ON system_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_admin_users_updated_at BEFORE UPDATE ON admin_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_admin_roles_updated_at BEFORE UPDATE ON admin_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================