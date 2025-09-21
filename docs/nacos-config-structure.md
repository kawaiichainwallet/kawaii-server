# Nacos配置中心结构设计

## 命名空间设计

### 环境划分
| 命名空间ID | 命名空间名称 | 描述 | 用途 |
|------------|--------------|------|------|
| kawaii-local | 本地开发环境 | 开发人员本地开发调试 | 本地数据库、Redis等 |
| kawaii-dev | 开发环境 | 团队联调和功能测试 | 开发服务器环境 |
| kawaii-test | 测试环境 | QA测试和自动化测试 | 测试服务器环境 |
| kawaii-staging | 预发布环境 | 生产前最后验证 | 准生产环境 |
| kawaii-prod | 生产环境 | 正式生产环境 | 生产服务器环境 |

## 配置文件结构

### 每个命名空间包含的配置文件

#### 1. kawaii-common.yml (公共配置)
包含所有微服务共享的配置：
- 数据库连接配置
- Redis配置
- 日志配置
- 基础业务配置
- 安全配置

#### 2. kawaii-gateway.yml (网关特定配置)
包含网关服务专属配置：
- 路由配置
- 网关过滤器配置
- 限流配置
- CORS配置

#### 3. kawaii-user.yml (用户服务特定配置)
包含用户服务专属配置：
- 用户业务配置
- 认证相关配置
- OTP配置
- 邮件/短信配置

#### 4. kawaii-core.yml (钱包核心服务配置)
包含钱包核心服务配置：
- 区块链配置
- 钱包业务配置
- Web3配置

#### 5. kawaii-payment.yml (支付服务配置)
包含支付服务配置：
- 支付业务配置
- 第三方支付配置

## 配置优先级

1. **环境变量** (最高优先级)
2. **Nacos配置中心** - 服务特定配置
3. **Nacos配置中心** - kawaii-common.yml
4. **application.yml** (最低优先级)

## 配置加载流程

```
应用启动
    ↓
读取application.yml (获取Nacos连接信息)
    ↓
连接对应命名空间的Nacos
    ↓
加载kawaii-common.yml (公共配置)
    ↓
加载服务特定配置 (如kawaii-gateway.yml)
    ↓
应用启动完成
```

## 环境切换方式

通过环境变量控制连接到不同的Nacos命名空间：

```bash
# 本地环境
NACOS_NAMESPACE=kawaii-local
NACOS_SERVER_ADDR=localhost:8848

# 开发环境
NACOS_NAMESPACE=kawaii-dev
NACOS_SERVER_ADDR=nacos-dev.company.com:8848

# 生产环境
NACOS_NAMESPACE=kawaii-prod
NACOS_SERVER_ADDR=nacos-prod.company.com:8848
```