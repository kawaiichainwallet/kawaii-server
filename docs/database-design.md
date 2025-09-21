# 数据库设计

## 数据库概览

### 技术选型

| 属性 | 值 |
|------|---|
| **数据库** | PostgreSQL 17 |
| **字符编码** | UTF8 |
| **时区** | UTC |
| **备份策略** | 定期全量 + 增量备份 |

## 微服务数据库架构

### 设计原则

本项目采用**数据库每服务一个**（Database per Service）的微服务架构模式，每个微服务拥有独立的数据库实例，确保：

- 🔒 **数据隔离**：服务间数据完全隔离，避免直接数据库访问
- 🚀 **独立扩展**：每个服务可独立扩展和优化数据库
- 🛡️ **故障隔离**：单个服务的数据库问题不影响其他服务
- 📦 **技术选择**：每个服务可选择最适合的数据库技术

### 数据库分配方案

| 数据库 | 微服务 | 端口 | 业务域 | 主要表 |
|--------|--------|------|--------|--------|
| `kawaii-user-db` | kawaii-user | 8091 | 用户身份管理、认证授权、个人资料 | users, user_profiles, user_kyc, jwt_blacklist |
| `kawaii-core-db` | kawaii-core | 8092 | 钱包管理、区块链资产、链上交易 | supported_chains, wallets, wallet_addresses, tokens, transactions, transaction_logs |
| `kawaii-payment-db` | kawaii-payment | 8093 | 支付处理、生活缴费 | payment_orders, bill_providers, bill_payments |
| `kawaii-merchant-db` | kawaii-merchant | 8094 | 商户管理、API配置 | merchants |
| `kawaii-notification-db` | kawaii-notification | 8095 | 消息通知、推送管理 | notifications |
| `kawaii-system-db` | kawaii-gateway | 8090 | 系统配置、审计追踪 | system_configs, audit_logs |

### 数据库脚本文件

所有微服务的数据库DDL脚本已独立创建，位于 `docs/database-scripts/` 目录：

- `kawaii-user-db.sql` - 用户服务数据库
- `kawaii-core-db.sql` - 钱包核心服务数据库
- `kawaii-payment-db.sql` - 支付服务数据库
- `kawaii-merchant-db.sql` - 商户服务数据库
- `kawaii-notification-db.sql` - 通知服务数据库
- `kawaii-system-db.sql` - 系统配置数据库

## 🚀 数据库部署

### 快速开始

#### 1. 创建数据库实例

```bash
# 为每个微服务创建独立的数据库
createdb kawaii_user_db
createdb kawaii_core_db
createdb kawaii_payment_db
createdb kawaii_merchant_db
createdb kawaii_notification_db
createdb kawaii_system_db
```

#### 2. 执行DDL脚本

```bash
# 进入脚本目录
cd docs/database-scripts/

# 用户服务数据库
psql -d kawaii_user_db -f kawaii-user-db.sql

# 钱包核心服务数据库
psql -d kawaii_core_db -f kawaii-core-db.sql

# 支付服务数据库
psql -d kawaii_payment_db -f kawaii-payment-db.sql

# 商户服务数据库
psql -d kawaii_merchant_db -f kawaii-merchant-db.sql

# 通知服务数据库
psql -d kawaii_notification_db -f kawaii-notification-db.sql

# 系统配置数据库
psql -d kawaii_system_db -f kawaii-system-db.sql
```

#### 3. 批量执行脚本

```bash
#!/bin/bash
# 批量初始化所有数据库

cd docs/database-scripts/

databases=(
    "kawaii_user_db:kawaii-user-db.sql"
    "kawaii_core_db:kawaii-core-db.sql"
    "kawaii_payment_db:kawaii-payment-db.sql"
    "kawaii_merchant_db:kawaii-merchant-db.sql"
    "kawaii_notification_db:kawaii-notification-db.sql"
    "kawaii_system_db:kawaii-system-db.sql"
)

for db_script in "${databases[@]}"; do
    IFS=':' read -r db_name script_file <<< "$db_script"
    echo "初始化数据库: $db_name"
    createdb "$db_name" 2>/dev/null || echo "数据库 $db_name 已存在"
    psql -d "$db_name" -f "$script_file"
done

echo "所有数据库初始化完成！"
```
